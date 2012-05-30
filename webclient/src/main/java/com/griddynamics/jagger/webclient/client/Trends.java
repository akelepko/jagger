package com.griddynamics.jagger.webclient.client;

import ca.nanometrics.gflot.client.*;
import ca.nanometrics.gflot.client.event.PlotHoverListener;
import ca.nanometrics.gflot.client.event.PlotItem;
import ca.nanometrics.gflot.client.event.PlotPosition;
import ca.nanometrics.gflot.client.jsni.Plot;
import ca.nanometrics.gflot.client.options.*;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.gwt.view.client.Range;
import com.griddynamics.jagger.webclient.client.dto.*;

import java.util.List;
import java.util.Set;

/**
 * @author "Artem Kirillov" (akirillov@griddynamics.com)
 * @since 5/28/12
 */
public class Trends extends Composite {
    interface TrendsUiBinder extends UiBinder<Widget, Trends> {
    }

    private static TrendsUiBinder uiBinder = GWT.create(TrendsUiBinder.class);

    private static final String INSTRUCTIONS = "Point your mouse to a data point on the chart";

    //Plot
    @UiField(provided = true)
    SimplePlot plot;

    @UiField(provided = true)
    Label hoverPoint = new Label(INSTRUCTIONS);

    @UiField
    Label cursorPosition;

    @UiField(provided = true)
    DataGrid<SessionDataDto> sessionsDataGrid;

    @UiField(provided = true)
    SimplePager sessionsPager;

    @UiField(provided = true)
    CellTree taskDetailsTree;

    private SessionDataAsyncDataProvider dataProvider = new SessionDataAsyncDataProvider();

    public Trends() {
        createPlot();
        setupTaskDetailsTree();
        setupDataGrid();
        setupPager();
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void createPlot() {
        PlotOptions plotOptions = new PlotOptions();
        plotOptions.setGlobalSeriesOptions(new GlobalSeriesOptions()
                .setLineSeriesOptions(new LineSeriesOptions().setLineWidth(1).setShow(true))
                .setPointsOptions(new PointsSeriesOptions().setRadius(2).setShow(true)).setShadowSize(0d));

        // Make the grid hoverable
        plotOptions.setGridOptions(new GridOptions().setHoverable(true));

        // create a series
//        SeriesHandler handler = model.addSeries("Ottawa's Month Temperatures (Daily Average in &deg;C)", "#007f00");

        // create the plot
        plot = new SimplePlot(plotOptions);

        final PopupPanel popup = new PopupPanel();
        final Label label = new Label();
        popup.add(label);

        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                if (item != null) {
                    String text = "x: " + item.getDataPoint().getX() + ", y: " + item.getDataPoint().getY();

                    hoverPoint.setText(text);

                    label.setText(text);
                    popup.setPopupPosition(item.getPageX() + 10, item.getPageY() - 25);
                    popup.show();
                } else {
                    hoverPoint.setText(INSTRUCTIONS);
                    popup.hide();
                }
            }
        }, false);
    }

    private void setupDataGrid() {
        sessionsDataGrid = new DataGrid<SessionDataDto>();
        sessionsDataGrid.setPageSize(15);
        sessionsDataGrid.setEmptyTableWidget(new Label("No Sessions"));

        // Add a selection model so we can select cells.
        final SelectionModel<SessionDataDto> selectionModel = new SingleSelectionModel<SessionDataDto>(new ProvidesKey<SessionDataDto>() {
            @Override
            public Object getKey(SessionDataDto item) {
                return item.getSessionId();
            }
        });
        sessionsDataGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager.<SessionDataDto>createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SessionSelectChangeHandler());

        // Checkbox column. This table will uses a checkbox column for selection.
        // Alternatively, you can call dataGrid.setSelectionEnabled(true) to enable mouse selection.
        Column<SessionDataDto, Boolean> checkColumn =
                new Column<SessionDataDto, Boolean>(new CheckboxCell(true, false)) {
                    @Override
                    public Boolean getValue(SessionDataDto object) {
                        // Get the value from the selection model.
                        return selectionModel.isSelected(object);
                    }
                };
        sessionsDataGrid.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        sessionsDataGrid.setColumnWidth(checkColumn, 40, Style.Unit.PX);

        Column nameColumn = new TextColumn<SessionDataDto>() {
            @Override
            public String getValue(SessionDataDto object) {
                return object.getName();
            }
        };
        sessionsDataGrid.addColumn(nameColumn, "Name");

        sessionsDataGrid.addColumn(new TextColumn<SessionDataDto>() {
            @Override
            public String getValue(SessionDataDto object) {
                return object.getStartDate().toString();
            }
        }, "Start Date");

        sessionsDataGrid.addColumn(new TextColumn<SessionDataDto>() {
            @Override
            public String getValue(SessionDataDto object) {
                return object.getEndDate().toString();
            }
        }, "End Date");

        dataProvider.addDataDisplay(sessionsDataGrid);
    }

    private void setupPager() {
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        sessionsPager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
        sessionsPager.setDisplay(sessionsDataGrid);
    }

    private void setupTaskDetailsTree() {
        CellTree.Resources res = GWT.create(CellTree.BasicResources.class);
        final MultiSelectionModel<PlotNameDto> selectionModel = new MultiSelectionModel<PlotNameDto>();
        taskDetailsTree = new CellTree(new WorkloadTaskDetailsTreeViewModel(selectionModel), null, res);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<PlotNameDto> selected = ((MultiSelectionModel<PlotNameDto>) event.getSource()).getSelectedSet();

                if (!selected.isEmpty()) {
                    PlotNameDto plotNameDto = selected.iterator().next();
                    PlotProviderService.Async.getInstance().getThroughputData(plotNameDto.getTaskId(), new AsyncCallback<List<PointDto>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error is occurred during server request processing (Throughput data fetching)");
                        }

                        @Override
                        public void onSuccess(List<PointDto> result) {
                            PlotModel plotModel = plot.getModel();
                            SeriesHandler handler = plotModel.addSeries("Montana's Month Temperatures (Daily Average in &deg;C)", "#007f00");

                            for (PointDto pointDto : result) {
                                handler.add(new DataPoint(pointDto.getX(), pointDto.getY()));
                            }

                            plot.redraw();
                        }
                    });
                }
            }
        });
    }

    //==========Nested Classes

    /**
     * Fetches all sessions from server
     */
    private static class SessionDataAsyncDataProvider extends AsyncDataProvider<SessionDataDto> {
        @Override
        protected void onRangeChanged(HasData<SessionDataDto> display) {
            Range range = display.getVisibleRange();
            final int start = range.getStart();
            int end = start + range.getLength();

            SessionDataServiceAsync sessionDataService = SessionDataService.Async.getInstance();
            AsyncCallback<PagedSessionDataDto> callback = new AsyncCallback<PagedSessionDataDto>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error is occurred during server request processing (Session data fetching)");
                }

                @Override
                public void onSuccess(PagedSessionDataDto result) {
                    updateRowData(start, result.getSessionDataDtoList());
                    updateRowCount(result.getTotalSize(), true);
                }
            };
            sessionDataService.getAll(start, range.getLength(), callback);
        }
    }

    /**
     * Handles select session event
     */
    private class SessionSelectChangeHandler implements SelectionChangeEvent.Handler {
        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            SessionDataDto selected = ((SingleSelectionModel<SessionDataDto>) event.getSource()).getSelectedObject();

            final WorkloadTaskDetailsTreeViewModel workloadTaskDetailsTreeViewModel = (WorkloadTaskDetailsTreeViewModel) taskDetailsTree.getTreeViewModel();
            final ListDataProvider<TaskDataDto> taskDataProvider = workloadTaskDetailsTreeViewModel.getTaskDataProvider();
            if (selected != null) {
                TaskDataService.Async.getInstance().getTaskDataForSession(selected.getSessionId(), new AsyncCallback<List<TaskDataDto>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error is occurred during server request processing (Task data fetching)");
                    }

                    @Override
                    public void onSuccess(List<TaskDataDto> result) {
                        taskDataProvider.getList().clear();
                        taskDataProvider.getList().addAll(result);

                        final MultiSelectionModel<PlotNameDto> plotNameSelectionModel = (MultiSelectionModel<PlotNameDto>) workloadTaskDetailsTreeViewModel.getSelectionModel();
                        for (TaskDataDto taskDataDto : result) {
                            final ListDataProvider<PlotNameDto> plotNameDataProvider = ((WorkloadTaskDetailsTreeViewModel)
                                    taskDetailsTree.getTreeViewModel()).getPlotNameDataProvider(taskDataDto);

                            PlotProviderService.Async.getInstance().getPlotListForTask(taskDataDto.getId(), new AsyncCallback<List<PlotNameDto>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Error is occurred during server request processing (Plot names for task fetching)");
                                }

                                @Override
                                public void onSuccess(List<PlotNameDto> result) {
                                    plotNameDataProvider.getList().clear();
                                    plotNameDataProvider.getList().addAll(result);

                                    for (PlotNameDto plotNameDto : plotNameSelectionModel.getSelectedSet()) {
                                        plotNameSelectionModel.setSelected(plotNameDto, false);
                                    }

                                    int childCount = taskDetailsTree.getRootTreeNode().getChildCount();
                                    for (int i=0; i< childCount; i++) {
                                        taskDetailsTree.getRootTreeNode().setChildOpen(i, false);
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                taskDataProvider.getList().clear();
                taskDataProvider.getList().add(WorkloadTaskDetailsTreeViewModel.getNoTasksDummyNode());
            }
        }
    }

}