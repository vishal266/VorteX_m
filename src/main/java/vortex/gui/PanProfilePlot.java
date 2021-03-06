/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vortex.gui;

import sandbox.clustering.Cluster;
import sandbox.clustering.ClusterMember;
import sandbox.clustering.ClusterSet;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import samusik.glasscmp.GlassBorder;

/**
 *
 * @author Nikolay Samusik
 */
public class PanProfilePlot extends javax.swing.JPanel implements PropertyChangeListener {

    private JFreeChart chart;
    private DefaultCategoryDataset graphDS = new DefaultCategoryDataset();
    private DefaultCategoryDataset quantileDS = new DefaultCategoryDataset();
    private ChartPanel chartPane;
    private ClusterSet cs;

    private Thread t;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (t != null) {
            t.interrupt();
        }
        t = new Thread(() -> {
            updateColors();
            chartPane.invalidate();
            chartPane.repaint();
        });
        t.start();
    }

    public void clear() {
        for (Cluster c : clusters) {
            c.removePropertyChangeListener(this);
        }
        clusters.clear();
        graphDS.clear();
    }

    /**
     * Creates new form panProfilePlot
     */
    public PanProfilePlot(ClusterSet cs,boolean legend) {
        initComponents();

        this.cs = cs;

        chart = ChartFactory.createLineChart(
                "Profiles", // chart title
                "Parameters", // domain axis label
                "Value", // range axis label
                graphDS, // data
                PlotOrientation.VERTICAL, // orientation
                legend, // include legend
                true, // tooltips
                false // urls
        );
        chartPane = new ChartPanel(chart, true, true, true, true, true);
        chart.setBackgroundPaint(new Color(255, 255, 255));
        chartPane.setOpaque(true);
        chartPane.setBorder(new GlassBorder());
        chartPane.setBackground(new Color(255, 255, 255, 255));
        chartPane.setInitialDelay(10);
        chart.getCategoryPlot().setBackgroundPaint(new Color(255, 255, 255));

        this.add(chartPane);

        String[] s = cs.getDataset().getFeatureNamesCombined();

        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
        chart.getCategoryPlot().getDomainAxis().setMaximumCategoryLabelWidthRatio(0.5f);
        if(chart.getLegend()!=null){
            chart.getLegend().setItemFont(chart.getLegend().getItemFont().deriveFont(9.0f));
        }
        chart.getCategoryPlot().setDataset(1, quantileDS);
        LineAndShapeRenderer br = new LineAndShapeRenderer();

        chart.getCategoryPlot().setRenderer(0, br);
        chart.setBorderPaint(Color.WHITE);
        br.setItemMargin(0.1);

    }

    private ArrayList<Cluster> clusters;

    private void updateColors() {
        for (int i = 0; i < clusters.size() && !Thread.interrupted(); i++) {
            final Cluster c = clusters.get(i);

            chart.getCategoryPlot().getRendererForDataset(graphDS).setSeriesPaint(i,
                    c.isSelected() ? c.getColorCode() : new Color(0, 0, 0, 10));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private int batchesAdded = 0;

    public void addClusterMembers(ClusterMember[] batch) {
        return;
    }

    public void addClusters(Cluster[] cl) {
        for (Cluster c : cl) {
            System.out.println("Adding cluster: " + c.toString());
            //c.addPropertyChangeListener(this);
            if (c == null) {
                return;
            }
            //c.addPropertyChangeListener(this);
            if (clusters == null) {
                clusters = new ArrayList<>();
            }
            clusters.add(c);

            for (int i = 0; i < cs.getDataset().getFeatureNames().length; i++) {
                double[] avgVec = c.getMode().getVector();
                graphDS.addValue(avgVec[i], c, i + 1 + " " + cs.getDataset().getFeatureNames()[i]);
            }

            if (cs.getDataset().getSideVarNames() != null) {
                for (int i = 0; i < c.getMode().getSideVector().length; i++) {
                    double[] avgVec = c.getMode().getSideVector();
                    graphDS.addValue(avgVec[i], c, (cs.getDataset().getNumDimensions() + i + 1) + " " + cs.getDataset().getSideVarNames()[i]);
                }
            }
        }
        updateColors();
    }

    public void removeCluster(Cluster c) {
        if (clusters != null) {
            clusters.remove(c);
        }
    }

    /*
    public void addCluster(Cluster c, Annotation ann) {
        if (c == null && ann == null) {
            return;
        }
        for (String term : ann.getTerms()) {
            ArrayList<Integer> dpIDsAssayAL = new ArrayList<>();
            int[] pid = ann.getDpIDsForTerm(term);
            // int[] dpids = new int[pid.length];
            for (int i = 0; i < pid.length; i++) {
                Datapoint dp = ann.getBaseDataset().getDPByID(pid[i]);
                if (dp != null) {
                    dpIDsAssayAL.add(dp.getID());
                }
            }
            int[] dpIDsAssay = new int[dpIDsAssayAL.size()];
            for (int i = 0; i < dpIDsAssay.length; i++) {
                dpIDsAssay[i] = dpIDsAssayAL.get(i);
            }
            Arrays.sort(dpIDsAssay);
            double count = 0;
            double[] avgFeatureVec = new double[c.getMode().getVector().length];
            double[] avgSideVec = new double[c.getMode().getSideVector().length];
            try {
                LinkedList<Datapoint> dp = new LinkedList<>();
                for (ClusterMember cm : c.getClusterMembers()) {
                    if (Arrays.binarySearch(dpIDsAssay, cm.getDatapoint().getID()) >= 0) {
                        count++;
                        avgFeatureVec = MatrixOp.sum(avgFeatureVec, cm.getDatapoint().getVector());
                        avgSideVec = MatrixOp.sum(avgSideVec, cm.getDatapoint().getSideVector());
                        dp.add(cm.getDatapoint());
                    }
                }
                MatrixOp.mult(avgFeatureVec, 1.0 / (double) count);
                MatrixOp.mult(avgSideVec, 1.0 / (double) count);
                logger.print(term, Arrays.toString(avgFeatureVec), Arrays.toString(avgSideVec));
                if (count > 0) {
                    String name = "id" + c.getID() + " (" + ((int) count) + ") - " + term;
                    Cluster clus = new Cluster(dp.toArray(new Datapoint[dp.size()]), avgFeatureVec, avgSideVec, name, null);
                    clus.setComment(name);
                    clus.setClusterSet(c.getClusterSet());
                    addCluster(clus);
                }
            } catch (SQLException e) {
                logger.showException(e);
            }
        }
    }
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
