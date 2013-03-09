/*
 * AdvertiserOverviewMetricsPanel.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package edu.umich.eecs.tac.viewer.role.adx;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import se.sics.isl.transport.Transportable;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Patrick R. Jordan
 */
public class AdNetOverviewMetricsPanel extends JPanel {
	private final AdvertiserMetricsModel model;

	public AdNetOverviewMetricsPanel(final TACAASimulationPanel simulationPanel) {
		model = new AdvertiserMetricsModel(simulationPanel);

		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(1, 1));
		setBorder(BorderFactory.createTitledBorder("Advertiser Information"));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		MetricsNumberRenderer renderer = new MetricsNumberRenderer();
		JTable table = new JTable(model);
		for (int i = 2; i < 4; i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
	}

	private static class AdvertiserMetricsModel extends AbstractTableModel {
		private static final String[] COLUMN_NAMES = new String[] { "Agent",
				"Profit", "VPC", "ROI" };

		List<AdvertiserMetricsItem> data;

		Map<Integer, AdvertiserMetricsItem> agents;

		private AdvertiserMetricsModel(
				final TACAASimulationPanel simulationPanel) {
			data = new ArrayList<AdvertiserMetricsItem>();
			agents = new HashMap<Integer, AdvertiserMetricsItem>();

			simulationPanel.addViewListener(new ViewAdaptor() {
				@Override
				public void participant(int agent, int role, String name,
						int participantID) {
					if (role == TACAdxConstants.ADVERTISER) {
						if (!agents.containsKey(agent)) {
							AdvertiserMetricsItem item = new AdvertiserMetricsItem(
									agent, name, AdvertiserMetricsModel.this,
									simulationPanel);
							agents.put(agent, item);
							data.add(item);
							fireTableDataChanged();
						}
					}
				}
			});
		}

		public void fireUpdatedAgent(int agent) {
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).getAgent() == agent) {
					fireTableRowsUpdated(i, i);
				}
			}
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			if (columnIndex == 0) {
				return data.get(rowIndex).getAdvertiser();
			} else if (columnIndex == 1) {
				return data.get(rowIndex).getProfit();
			} else if (columnIndex == 2) {
				return data.get(rowIndex).getVPC();
			} else if (columnIndex == 3) {
				return data.get(rowIndex).getROI();
			}

			return null;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}
	}

	private static class AdvertiserMetricsItem {
		private final int agent;
		private final String advertiser;

		private int impressions;
		private int clicks;
		private int conversions;
		private double revenue;
		private double cost;

		private AdvertiserInfo advertiserInfo;

		private final AdvertiserMetricsModel model;

		private AdvertiserMetricsItem(int agent, String advertiser,
				AdvertiserMetricsModel model,
				TACAASimulationPanel simulationPanel) {
			this.agent = agent;
			this.advertiser = advertiser;
			this.model = model;

			simulationPanel.addViewListener(new DataUpdateListener(this));
		}

		public int getAgent() {
			return agent;
		}

		public String getAdvertiser() {
			return advertiser;
		}

		public double getProfit() {
			return revenue - cost;
		}

		public double getCapacity() {
			return advertiserInfo != null ? advertiserInfo
					.getDistributionCapacity() : Double.NaN;
		}

		public double getCTR() {
			return impressions > 0 ? ((double) clicks) / impressions
					: Double.NaN;
		}

		public double getConvRate() {
			return clicks > 0 ? ((double) conversions) / clicks : Double.NaN;
		}

		public double getCPC() {
			return cost / clicks;
		}

		public double getCPM() {
			return 1000.0 * cost / (impressions);
		}

		public double getVPC() {
			return (revenue - cost) / clicks;
		}

		public double getROI() {
			return (revenue - cost) / cost;
		}

		protected void addRevenue(double revenue) {
			this.revenue += revenue;

			model.fireUpdatedAgent(agent);
		}

		protected void addCost(double cost) {
			this.cost += cost;

			model.fireUpdatedAgent(agent);
		}

		protected void addImpressions(int impressions) {
			this.impressions += impressions;

			model.fireUpdatedAgent(agent);
		}

		protected void addClicks(int clicks) {
			this.clicks += clicks;
		}

		protected void addConversions(int conversions) {
			this.conversions += conversions;

			model.fireUpdatedAgent(agent);
		}

		public void setAdvertiserInfo(AdvertiserInfo advertiserInfo) {
			this.advertiserInfo = advertiserInfo;

			model.fireUpdatedAgent(agent);
		}

		public AdvertiserInfo getAdvertiserInfo() {
			return advertiserInfo;
		}
	}

	private static class DataUpdateListener extends ViewAdaptor {
		private final AdvertiserMetricsItem item;

		private DataUpdateListener(AdvertiserMetricsItem item) {
			this.item = item;
		}

		@Override
		public void dataUpdated(final int agent, final int type, final int value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (agent == item.getAgent()) {
						switch (type) {
						case TACAdxConstants.DU_IMPRESSIONS:
							item.addImpressions(value);
							break;
						case TACAdxConstants.DU_CLICKS:
							item.addClicks(value);
							break;
						case TACAdxConstants.DU_CONVERSIONS:
							item.addConversions(value);
							break;
						}
					}
				}
			});

		}

		@Override
		public void dataUpdated(final int agent, final int type,
				final Transportable value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (agent == item.getAgent()) {
						switch (type) {
						case TACAdxConstants.DU_SALES_REPORT:
							handleSalesReport((SalesReport) value);
							break;
						case TACAdxConstants.DU_AD_NETWORK_REPORT:
							handleAdNetworkReport((AdNetworkReport) value);
							break;
						case TACAdxConstants.DU_ADVERTISER_INFO:
							handleAdvertiserInfo((AdvertiserInfo) value);
							break;
						}
					}
				}
			});
		}

		private void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
			item.setAdvertiserInfo(advertiserInfo);
		}

		private void handleAdNetworkReport(AdNetworkReport report) {
			item.addCost(report.getDailyCost());

		}

		private void handleSalesReport(SalesReport salesReport) {
			double revenue = 0.0;

			for (int i = 0; i < salesReport.size(); i++) {
				revenue += salesReport.getRevenue(i);
			}
			item.addRevenue(revenue);
		}
	}

	public class MetricsNumberRenderer extends JLabel implements
			TableCellRenderer {

		public MetricsNumberRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object object, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}

			setHorizontalAlignment(JLabel.RIGHT);
			setText(String.format("%.2f", object));

			return this;
		}
	}
}