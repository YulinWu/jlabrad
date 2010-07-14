package org.labrad.grapher.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ca.nanometrics.gflot.client.DataPoint;
import ca.nanometrics.gflot.client.PlotModel;
import ca.nanometrics.gflot.client.SeriesHandler;
import ca.nanometrics.gflot.client.SeriesType;
import ca.nanometrics.gflot.client.SimplePlot;
import ca.nanometrics.gflot.client.options.PointsSeriesOptions;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

public class DatasetView extends Composite {
  private List<String> path;
  private int num;
  private DatasetInfo info;
  private final VerticalPanel panel = new VerticalPanel();
  private final DataVaultServiceAsync service;
  
  public DatasetView(List<String> path, int num, DataVaultServiceAsync service) {
    this.path = path;
    this.num = num;
    
    initWidget(panel);
    
    this.service = service;
    this.service.getDatasetInfo(path, num, new AsyncCallback<DatasetInfo>() {

      @Override
      public void onFailure(Throwable caught) {
        DialogBox msg = new DialogBox();
        msg.setText("Error occurred while grabbing directory listing:\n\n" + caught.getMessage());
        msg.show();
      }

      @Override
      public void onSuccess(DatasetInfo result) {
        handleDatasetInfo(result);
      }
      
    });
  }
  
  public void handleDatasetInfo(DatasetInfo info) {
    this.info = info;
    
    DisclosurePanel paramPanel = new DisclosurePanel(info.getName());
    VerticalPanel contents = new VerticalPanel();
    contents.add(new Label(info.getIndependents().toString()));
    contents.add(new Label(info.getDependents().toString()));
    
    Map<String, String> params = info.getParameters();
    Grid t = new Grid(params.size(), 2);
    int i = 0;
    List<String> keyList = new ArrayList<String>(params.keySet());
    String[] keys = new String[keyList.size()];
    for (int j = 0; j < keyList.size(); j++) {
      keys[j] = keyList.get(j);
    }
    Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
    for (String key : keys) {
      t.setText(i, 0, key);
      t.setText(i, 1, params.get(key));
      i++;
    }
    contents.add(t);
    
    paramPanel.add(contents);
    panel.clear();
    panel.add(paramPanel);
    
    service.getData(path, info.getNum(), new AsyncCallback<double[][]>() {

      @Override
      public void onFailure(Throwable caught) {
        final DialogBox msg = new DialogBox();
        msg.setModal(true);
        msg.setText("Error occurred while grabbing data:\n\n" + caught.getMessage());
        Button ok = new Button("OK");
        ok.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            msg.hide();
          }
        });
        msg.setWidget(ok);
        msg.show();
      }

      @Override
      public void onSuccess(double[][] result) {
        handleData(result);
      }
      
    });
  }
  
  final boolean USE_GFLOT = true;
  
  public void handleData(double[][] data) {
    if (info.getIndependents().size() == 1) {
      if (USE_GFLOT) {
        // create plot with gflot
        PlotModel model = new PlotModel();
        for (int i = 0; i < info.getDependents().size(); i++) {
          SeriesHandler handler = model.addSeries(info.getDependents().get(i));
          handler.setOptions(SeriesType.POINTS, (new PointsSeriesOptions()).setRadius(4).setFill(true));
          for (int j = 0; j < data.length; j++) {
            handler.add(new DataPoint(data[j][0], data[j][i+1]));
          }
        }
        SimplePlot plot = new SimplePlot(model);
        plot.setWidth(800);
        plot.setHeight(600);
        panel.add(plot);
      } else {
        // draw 1D data
        GWTCanvas canvas = new GWTCanvas(800, 600);
        drawData1D(canvas, data);
        panel.add(canvas);
      }
    } else if (info.getIndependents().size() == 2) {
      // draw 2D data
      GWTCanvas canvas = new GWTCanvas(800, 600);
      drawData2D(canvas, data);
      panel.add(canvas);
    }
  }
  
  private void drawData1D(GWTCanvas canvas, double[][] data) {
    int n = data.length;
    int nDeps = info.getDependents().size();
    double[] xs = new double[n];
    double[][] yss = new double[nDeps][n];
    double xMin = Double.MAX_VALUE;
    double xMax = Double.MIN_VALUE;
    double yMin = Double.MAX_VALUE;
    double yMax = Double.MIN_VALUE;
    
    if (n == 0) {
      xMin = 0;
      xMax = 1;
      yMin = 0;
      yMax = 1; 
    } else {
      for (int i = 0; i < n; i++) {
        double x = data[i][0];
        xs[i] = x;
        xMin = Math.min(x, xMin);
        xMax = Math.max(x, xMax);
        for (int j = 0; j < nDeps; j++) {
          double y = data[i][j+1];
          yss[j][i] = y;
          yMin = Math.min(y, yMin);
          yMax = Math.max(y, yMax);
        }
      }
    }
    if (n == 1) {
      xMin -= 0.5;
      xMax += 0.5;
      yMin -= 0.5;
      yMax += 0.5;
    }
    
    for (int j = 0; j < nDeps; j++) {
      double[] ys = yss[j];
      Color c = getColor(j);
      canvas.setStrokeStyle(c);
      canvas.setFillStyle(c);

      canvas.setLineWidth(1);      
      canvas.beginPath();
      for (int i = 0; i < n; i++) {        
        double x = (xs[i] - xMin) / (xMax - xMin) * canvas.getCoordWidth();
        double y = (yMax - ys[i]) / (yMax - yMin) * canvas.getCoordHeight();

        if (i == 0) {
          canvas.moveTo(x, y);
        } else {
          canvas.lineTo(x, y);
        }
      }
      canvas.stroke();

      for (int i = 0; i < n; i++) {        
        double x = (xs[i] - xMin) / (xMax - xMin) * canvas.getCoordWidth();
        double y = (yMax - ys[i]) / (yMax - yMin) * canvas.getCoordHeight();

        canvas.beginPath();
        canvas.arc(x, y, 3, 0, 2*Math.PI, false);
        canvas.fill();
      }

    }
  }
  
  private void drawData2D(GWTCanvas canvas, double[][] data) {
    
  }
  
  private Color getColor(int i) {
    switch (i % 8) {
    case 0: return Color.RED;
    case 1: return Color.GREEN;
    case 2: return Color.BLUE;
    case 3: return Color.CYAN;
    case 4: return Color.BLUEVIOLET;
    case 5: return Color.ORANGE;
    case 6: return Color.GREY;
    case 7: return Color.BLACK;
    }
    return Color.GREY;
  }
}
