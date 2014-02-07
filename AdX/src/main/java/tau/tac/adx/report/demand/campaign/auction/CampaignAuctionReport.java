package tau.tac.adx.report.demand.campaign.auction;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

/**
 * 
 * @author Tomer Greenwald
 * 
 */
public class CampaignAuctionReport extends
		AbstractKeyedEntryList<CampaignAuctionReportKey, CampaignAuctionReportEntry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -383908225939942653L;
	
	/** Campaign id key. */
	private static String CAMPAIGN_ID_KEY = "CAMPAIGN_ID_KEY"; 
	
	/** Campaign ID. */
	private int campaignID;

	/**
	 * Default constructor.
	 */
	public CampaignAuctionReport() {
		super();
	}

	/**
	 * @param campaignID
	 */
	public CampaignAuctionReport(int campaignID) {
		super();
		this.campaignID = campaignID;
	}

	@Override
	protected CampaignAuctionReportEntry createEntry(CampaignAuctionReportKey key) {
		return new CampaignAuctionReportEntry(key);
	}

	@Override
	protected final Class<CampaignAuctionReportEntry> entryClass() {
		return CampaignAuctionReportEntry.class;
	}

	public String toMyString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (CampaignAuctionReportEntry entry : getEntries()) {
			stringBuilder.append(entry.getKey()).append(" : ").append(entry)
					.append("\n");
		}
		return "CampaignReport: " + stringBuilder;
	}

	/**
	 * Adds an {@link campaignReportKey} to the report.
	 * 
	 * @param campaignReportKey
	 *            {@link campaignReportKey}.
	 * @return {@link CampaignAuctionReportEntry}.
	 * 
	 */
	public CampaignAuctionReportEntry addReportEntry(
			CampaignAuctionReportKey campaignReportKey) {
		lockCheck();
		int index = addKey(campaignReportKey);
		return getEntry(index);
	}

	/**
	 * Retrieves a {@link CampaignAuctionReportEntry} keyed with a
	 * {@link campaignReportKey}.
	 * 
	 * @param campaignReportKey
	 *            {@link campaignReportKey}.
	 * @return {@link CampaignAuctionReportEntry}.
	 * 
	 */
	public CampaignAuctionReportEntry getCampaignReportEntry(
			CampaignAuctionReportKey campaignReportKey) {
		return getEntry(campaignReportKey);
	}
	
	@Override
	protected void readBeforeEntries(TransportReader reader)
			throws ParseException {
		campaignID = reader.getAttributeAsInt(CAMPAIGN_ID_KEY);
	}
	
	@Override
	protected void writeBeforeEntries(TransportWriter writer) {
		writer.attr(CAMPAIGN_ID_KEY, campaignID);
	}

}