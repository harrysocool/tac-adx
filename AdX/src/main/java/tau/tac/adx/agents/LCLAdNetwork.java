package tau.tac.adx.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.props.ReservePriceInfo;
//import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BankStatus;

/**
 * 
 * @author Mariano Schain Test plug-in
 * 
 */
public class LCLAdNetwork extends Agent {

	private final Logger log = Logger
			.getLogger(LCLAdNetwork.class.getName());

	/*
	 * Basic simulation information. An agent should receive the {@link
	 * StartInfo} at the beginning of the game or during recovery.
	 */
	@SuppressWarnings("unused")
	private StartInfo startInfo;

	/**
	 * Messages received:
	 * 
	 * We keep all the {@link CampaignReport} campaign reports delivered to the
	 * agent. We also keep the initialization messages {@link PublisherCatalog}
	 * and {@link InitialCampaignMessage} and the most recent messages and
	 * reports {@link CampaignOpportunityMessage}, {@link CampaignReport}, and
	 * {@link AdNetworkDailyNotification}.
	 */
	private final Queue<CampaignReport> campaignReports;
	private PublisherCatalog publisherCatalog;
	private InitialCampaignMessage initialCampaignMessage;
	private CampaignOpportunityMessage campaignOpportunityMessage;
	private CampaignReport campaignReport;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	/*
	 * The addresses of server entities to which the agent should send the daily
	 * bids data
	 */
	private String demandAgentAddress;
	private String adxAgentAddress;

	/*
	 * we maintain a list of queries - each characterized by the web site (the
	 * publisher), the device type, the ad type, and the user market segment
	 */
	private AdxQuery[] queries;

	/**
	 * Information regarding the latest campaign opportunity announced
	 */
	private CampaignData pendingCampaign;

	/**
	 * We maintain a collection (mapped by the campaign id) of the campaigns won
	 * by our agent.
	 */
	private Map<Integer, CampaignData> myCampaigns;
	private ArrayList<String> pubNames = new ArrayList<String>();

	/*
	 * the bidBundle to be sent daily to the AdX
	 */
	private AdxBidBundle bidBundle;

	/*
	 * The current bid level for the user classification service
	 */
	private double ucsBid;

	/*
	 * The qualityScore for the quality level
	 */
	private double qualityScore;
	private double min_cmpBidMillis;
	private double max_cmpBidMillis;
	private double cmpBidfactor;
	private double lastBid;

	/*
	 * The targeted service level for the user classification service
	 */
	private double ucsTargetLevel;

	/*
	 * current day of simulation
	 */
	private int day;
	private String[] publisherNames;
	private CampaignData currCampaign;
	private double greedyfactor = 1;
	long cmpBidMillis = 0;
	
	/*
	 * current day of simulation
	 */
	private boolean activeCampaign;
	
	public LCLAdNetwork() {
		campaignReports = new LinkedList<CampaignReport>();
//		System.out.println(campaignReports.toString());
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();

//			 log.info("===============================================================");

			if (content instanceof InitialCampaignMessage) {
				handleInitialCampaignMessage((InitialCampaignMessage) content);
			} else if (content instanceof CampaignOpportunityMessage) {
				handleCampaignOpportunityMessage((CampaignOpportunityMessage) content);
			} else if (content instanceof CampaignReport) {
				handleCampaignReport((CampaignReport) content);
			} else if (content instanceof AdNetworkDailyNotification) {
				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
			} else if (content instanceof AdxPublisherReport) {
				handleAdxPublisherReport((AdxPublisherReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);
			} else if (content instanceof PublisherCatalog) {
				handlePublisherCatalog((PublisherCatalog) content);
			} else if (content instanceof AdNetworkReport) {
				handleAdNetworkReport((AdNetworkReport) content);
			} else if (content instanceof StartInfo) {
				handleStartInfo((StartInfo) content);
			} else if (content instanceof BankStatus) {
				handleBankStatus((BankStatus) content);
//			} else if (content instanceof CampaignAuctionReport) {
//				hadnleCampaignAuctionReport((CampaignAuctionReport) content);
			} else if (content instanceof ReservePriceInfo) {
				handleReservePriceInfo ((ReservePriceInfo) content);
			} else {
				System.out.println("UNKNOWN Message Received: " + content);
			}

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE,
					"Exception thrown while trying to parse message." + e);
			return;
		}
	}

	private void handleReservePriceInfo(ReservePriceInfo content) {
		// ingoring - this message is obsolete
		log.info("Day " + day + " :" + content.toString());
	}

	private void handleBankStatus(BankStatus content) {
		log.info("Day " + day + " :" + content.toString());
	}

	/**
	 * Processes the start information.
	 * 
	 * @param startInfo
	 *            the start information.
	 */
	protected void handleStartInfo(StartInfo startInfo) {
		this.startInfo = startInfo;
	}

	/**
	 * Process the reported set of publishers
	 * 
	 * @param publisherCatalog
	 */
	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		/*generate all possible impression opportunity*/
		generateAdxQuerySpace();
		/*generate 6 Publisher Name*/
		getPublishersNames();
	}

	/**
	 * On day 0, a campaign (the "initial campaign") is allocated to each
	 * competing agent. The campaign starts on day 1. The address of the
	 * server's AdxAgent (to which bid bundles are sent) and DemandAgent (to
	 * which bids regarding campaign opportunities may be sent in subsequent
	 * days) are also reported in the initial campaign message
	 */
	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
//		System.out.println(campaignMessage.toString());

		day = 0;
		qualityScore = 1.0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);
		campaignData
				.setBudget(initialCampaignMessage.getBudgetMillis() / 1000.0);
		currCampaign = campaignData;
		genCampaignQueries(currCampaign);

		/*
		 * The initial campaign is already allocated to our agent so we add it
		 * to our allocated-campaigns list.
		 */
		log.info("Day " + day + ": Allocated campaign - "
				+ campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}

	/**
	 * On day n ( > 0) a campaign opportunity is announced to the competing
	 * agents. The campaign starts on day n + 2 or later and the agents may send
	 * (on day n) related bids (attempting to win the campaign). The allocation
	 * (the winner) is announced to the competing agents during day n + 1.
	 */
	private void handleCampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		campaignOpportunityMessage = com;
		day = com.getDay();

		pendingCampaign = new CampaignData(com);
		log.info("Day " + day + ": Campaign opportunity - "
				+ pendingCampaign);

		/*
		 * The campaign requires com.getReachImps() impressions. The competing
		 * Ad Networks bid for the total campaign Budget (that is, the ad
		 * network that offers the lowest budget gets the campaign allocated).
		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
		 * therefore the total number of impressions may be treated as a reserve
		 * (upper bound) price for the auction.
		 */

		Random random = new Random();

		long cmpimps = com.getReachImps();

		// long cmpBidMillis = random.nextInt((int) cmpimps);
		min_cmpBidMillis = cmpimps/qualityScore/10 + 1;
		max_cmpBidMillis = cmpimps*qualityScore - 1;

//		if(qualityScore >= 1){
//			double rnd = random.nextDouble();
//			if(rnd > 0.5){
//				cmpBidMillis = (long) max_cmpBidMillis;
//			}else {
//				cmpBidMillis = (long) randomInRange(min_cmpBidMillis,max_cmpBidMillis);
//			}		
//		}else {
//			double rnd = random.nextDouble();
//			if(rnd > 0.2){
//				cmpBidMillis = (long) min_cmpBidMillis;
//			}else {
//				cmpBidMillis = (long) randomInRange(min_cmpBidMillis,max_cmpBidMillis);
//			}		
//		}
		if(cmpimps * greedyfactor > max_cmpBidMillis){
			cmpBidMillis = (long) max_cmpBidMillis;
		}else if(cmpimps * greedyfactor < min_cmpBidMillis){
			cmpBidMillis = (long) min_cmpBidMillis;
		}else{
			cmpBidMillis = (long) (cmpimps * greedyfactor);
		}
		
		log.info("Day " + day
				+ ": Campaign total budget bid (millis): " + cmpBidMillis + "\n"
				+ "Greedyfactor:  " + greedyfactor
				);

		/*
		 * Adjust ucs bid s.t. target level is achieved. Note: The bid for the
		 * user classification service is piggybacked
		 */

		if (adNetworkDailyNotification != null) {
			double ucsLevel = adNetworkDailyNotification.getServiceLevel();
			/* Determine how much ucsBid to submit*/
			if((day >= currCampaign.dayEnd)) {
				ucsBid = 0;
			}else if(qualityScore <= 0.75){
				ucsBid = 0.2;
			}else{
				ucsBid = 0.1 + random.nextDouble() / 10.0;;
			}
			log.info("Day " + day + ": adNetwork Daily Notification: ucs level reported: "
					+ ucsLevel);
		} else {
			log.info("Day " + day + ": Initial ucs bid is " + ucsBid);
		}

		/* Note: Campaign bid is in millis */
		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.id,
				cmpBidMillis);
		sendMessage(demandAgentAddress, bids);
	}

//	private Random new Random() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	protected static Random random = new Random();
	
	public static double randomInRange(double min, double max) {
		  double range = max - min;
		  double scaled = random.nextDouble() * range;
		  double shifted = scaled + min;
		  return shifted; // == (rand.nextDouble() * (max-min)) + min;
		}

	/**
	 * On day n ( > 0), the result of the UserClassificationService and Campaign
	 * auctions (for which the competing agents sent bids during day n -1) are
	 * reported. The reported Campaign starts in day n+1 or later and the user
	 * classification service level is applicable starting from day n+1.
	 */
	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification notificationMessage) {

		adNetworkDailyNotification = notificationMessage;

		if(notificationMessage.getCostMillis() == 0){
			greedyfactor /= 1.2;
		}else if(notificationMessage.getCostMillis() == cmpBidMillis){
			greedyfactor *= 1;
		}else if(notificationMessage.getCostMillis() != cmpBidMillis){
			greedyfactor *= 1.5;
		}
		
		log.info("Day " + day + ": Daily Notification for campaign "
				+ adNetworkDailyNotification.getCampaignId()
				+ " allocated to " 
				+ notificationMessage.getWinner()
				);
		/* we won the pendingCampaign */
		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& (notificationMessage.getCostMillis() != 0)) {

			/* add campaign to list of won campaigns */
			pendingCampaign
					.setBudget(notificationMessage.getCostMillis() / 1000.0);
			currCampaign = pendingCampaign;
			/* ????????????????????????? */
			genCampaignQueries(currCampaign);
			myCampaigns.put(pendingCampaign.id, pendingCampaign);

			log.info( "Day " + day + ": " + " WON at cost (Millis)"
					+ notificationMessage.getCostMillis());
		}

		qualityScore = notificationMessage.getQualityScore();

		log.info("Day " + day + ": "
						+ "UCS Level: "
						+ notificationMessage.getServiceLevel() 
						+ " at price "
						+ notificationMessage.getPrice()
						+ " Quality Score is: "
						+ qualityScore);
	}

	/**
	 * The SimulationStatus message received on day n indicates that the
	 * calculation time is up and the agent is requested to send its bid bundle
	 * to the AdX.
	 */
	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		log.info("Day " + day + " : Simulation Status Received");
		sendBidAndAds();
		log.info("Day " + day + " ended. Starting next day");
		log.info("===============================================================");
		/* Next day start*/
		++day;
	}

	/**
	 * 
	 */
	protected void sendBidAndAds() {

		bidBundle = new AdxBidBundle();

		/*
		 * 
		 */
		for (CampaignData currCampaign : myCampaigns.values()) {
		
		int dayBiddingFor = day + 1;

		/* A random bid, fixed for all queries of the campaign */
		/*
		 * Note: bidding per 1000 imps (CPM) - no more than average budget
		 * revenue per imp
		 */
		double ibid;
		double impressionLimit;
		double budgetLimit;
		double impbidfactor;
		
		if(qualityScore <= 0.85){
			impressionLimit =  currCampaign.reachImps * 1.05;
			budgetLimit = currCampaign.budget;
			impbidfactor = 10;
		}else{
			impressionLimit = currCampaign.reachImps;
			budgetLimit = currCampaign.budget*0.8;
			impbidfactor = 5 + new Random().nextDouble() * 5;
		}
		
		
//		double rbid = 1000.0;
		/*
		 * add bid entries w.r.t. each active campaign with remaining contracted
		 * impressions.
		 * 
		 * for now, a single entry per active campaign is added for queries of
		 * matching target segment.
		 */

			if ((dayBiddingFor >= currCampaign.dayStart)
					&& (dayBiddingFor <= currCampaign.dayEnd)
					&& ((currCampaign.reachImps - currCampaign.impsTogo()) < impressionLimit)) {
	
				int entCount = 0;
				int weight = 0;
				if(dayBiddingFor <= currCampaign.dayStart + 2){
					ibid = 10000.0;
				}else if((dayBiddingFor >= currCampaign.dayEnd - 2)&&(currCampaign.impsTogo() > 0)){
					ibid = 10000.0;
				}else {
					ibid = 1000 * impbidfactor * currCampaign.budget / currCampaign.reachImps;
				}
				log.info("Impressions Remaining:    " + currCampaign.impsTogo());
				log.info("Impression Bid:           " + ibid);
				
				for (AdxQuery query : currCampaign.campaignQueries) {
					if (currCampaign.impsTogo() - entCount > 0) {
						/*
						 * among matching entries with the same campaign id, the AdX
						 * randomly chooses an entry according to the designated
						 * weight. by setting a constant weight 1, we create a
						 * uniform probability over active campaigns(irrelevant
						 * because we are bidding only on one campaign)
						 */
						if (query.getDevice() == Device.pc) {
							if (query.getAdType() == AdType.text) {
								entCount++;
								weight = 7;
							} else {
								entCount += currCampaign.videoCoef;
								weight = 7;
							}
						} else {
							if (query.getAdType() == AdType.text) {
								entCount += currCampaign.mobileCoef;
								weight = 7;
//								ibid = 0;
							} else {
								entCount += currCampaign.videoCoef
										+ currCampaign.mobileCoef;
								weight = 7;
//								ibid = 0;
							}
	
						}
						bidBundle.addQuery(query, ibid, new Ad(null),
								currCampaign.id, weight);
					}
				}
	
				
				bidBundle.setCampaignDailyLimit(currCampaign.id,
						(int) impressionLimit, budgetLimit);
	
				log.info("Day " + day + ": Updated " + entCount
						+ " Bid Bundle entries for Campaign id " + currCampaign.id);
			}
	
			if (bidBundle != null) {
				log.info("Day " + day + ": Sending BidBundle");
				sendMessage(adxAgentAddress, bidBundle);
			}
		}	
	}
	/**
	 * Campaigns performance w.r.t. each allocated campaign
	 */
	private void handleCampaignReport(CampaignReport campaignReport) {

		campaignReports.add(campaignReport);

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();
			myCampaigns.get(cmpId).setStats(cstats);
			log.info("Day " + day + ": Updating campaign " + cmpId
					+ " Ends at Day " + myCampaigns.get(cmpId).dayEnd
					+ " Reaches :" + myCampaigns.get(cmpId).reachImps
					+ " tgtImps "  + cstats.getTargetedImps() 
					+ " Cost of imps is " + cstats.getCost()
					+ " Bugets: " + myCampaigns.get(cmpId).budget
					);
		}
	}

	/**
	 * Users and Publishers statistics: popularity and ad type orientation
	 */
	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		log.info("Publishers Report: ");
		for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
			AdxPublisherReportEntry entry = adxPublisherReport
					.getEntry(publisherKey);
			log.info(entry.toString());
		}
	}

	/**
	 * 
	 * @param AdNetworkReport
	 */
	private void handleAdNetworkReport(AdNetworkReport adnetReport) {

		log.info("Day " + day + " : AdNetworkReport");
		for (AdNetworkKey adnetKey : adnetReport.keys()) {
			AdNetworkReportEntry entry = adnetReport .getAdNetworkReportEntry(adnetKey);
//			System.out.println("Publisher: " + adnetKey.getPublisher() 
//								+ " " + adnetKey.getAdType() + " " + adnetKey.getDevice()
//								+ " " + adnetKey.getIncome() + " " + adnetKey.getGender()
//								+ "************"
//								+ " BidCount: " + entry.getBidCount()
//								+ " Bidwin: " + entry.getWinCount()
//								+ " Bid Cost: " + entry.getCost()); 
		}
		log.info("DailyCost = "+ adnetReport.getDailyCost());
		
		/*
		 * for (AdNetworkKey adnetKey : adnetReport.keys()) {
		 * 
		 * double rnd = Math.random(); if (rnd > 0.95) { AdNetworkReportEntry
		 * entry = adnetReport .getAdNetworkReportEntry(adnetKey);
		 * System.out.println(adnetKey + " " + entry); } }
		 */
	}

	@Override
	protected void simulationSetup() {
		Random random = new Random();

		day = 0;
		bidBundle = new AdxBidBundle();

		/* initial bid between 0.1 and 0.2 */
		ucsBid = 0.1;

		myCampaigns = new HashMap<Integer, CampaignData>();
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		bidBundle = null;
	}

	/**
	 * A user visit to a publisher's web-site results in an impression
	 * opportunity (a query) that is characterized by the the publisher, the
	 * market segment the user may belongs to, the device used (mobile or
	 * desktop) and the ad type (text or video).
	 * 
	 * An array of all possible queries is generated here, based on the
	 * publisher names reported at game initialization in the publishers catalog
	 * message
	 */
	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> querySet = new HashSet<AdxQuery>();

			/*
			 * for each web site (publisher) we generate all possible variations
			 * of device type, ad type, and user market segment
			 */
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				String publishersName = publisherCatalogEntry
						.getPublisherName();
				for (MarketSegment userSegment : MarketSegment.values()) {
					Set<MarketSegment> singleMarketSegment = new HashSet<MarketSegment>();
					singleMarketSegment.add(userSegment);

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.video));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.video));

				}

				/**
				 * An empty segments set is used to indicate the "UNKNOWN"
				 * segment such queries are matched when the UCS fails to
				 * recover the user's segments.
				 */
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.text));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.text));
			}
			queries = new AdxQuery[querySet.size()];
			querySet.toArray(queries);
			for (AdxQuery arrays : querySet) {
//				log.info(arrays.toString());
			}
		}
	}
    
	/*
	 * genarates an array of the publishers names
	 */
	private void getPublishersNames() {
		if (null == publisherNames && publisherCatalog != null) {
			ArrayList<String> names = new ArrayList<String>();
			for (PublisherCatalogEntry pce : publisherCatalog) {
				names.add(pce.getPublisherName());
			}

			publisherNames = new String[names.size()];
			names.toArray(publisherNames);
			log.info("Publisher: " + names.toString()); 
		}
	}

	/*
	 * genarates the campaign queries relevant for the specific campaign, and
	 * assign them as the campaigns campaignQueries field
	 */
	private void genCampaignQueries(CampaignData campaignData) {
		Set<AdxQuery> campaignQueriesSet = new HashSet<AdxQuery>();
		for (String PublisherName : publisherNames) {
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.video));
		}

		campaignData.campaignQueries = new AdxQuery[campaignQueriesSet.size()];
		campaignQueriesSet.toArray(campaignData.campaignQueries);
		/* log the campaignData.campaignQueries */
		for (AdxQuery arrays : campaignData.campaignQueries) {
//			log.info(arrays.toString()ing());
		}
	}

	private class CampaignData {
		/* campaign attributes as set by server */
		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		private AdxQuery[] campaignQueries;// array of queries relvent for the
											// campaign.

		/* campaign info as reported */
		CampaignStats stats;
		double budget;

		public CampaignData(InitialCampaignMessage icm) {
			reachImps = icm.getReachImps();
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			targetSegment = icm.getTargetSegment();
			videoCoef = icm.getVideoCoef();
			mobileCoef = icm.getMobileCoef();
			id = icm.getId();

			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		public void setBudget(double d) {
			budget = d;
		}

		public CampaignData(CampaignOpportunityMessage com) {
			dayStart = com.getDayStart();
			dayEnd = com.getDayEnd();
			id = com.getId();
			reachImps = com.getReachImps();
			targetSegment = com.getTargetSegment();
			mobileCoef = com.getMobileCoef();
			videoCoef = com.getVideoCoef();
			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		@Override
		public String toString() {
			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
					+ dayEnd + " " + targetSegment + ", reach: " + reachImps
					+ " coefs: (v=" + videoCoef + ", m=" + mobileCoef + ")";
		}

		int impsTogo() {
			return (int) Math.max(0, reachImps - stats.getTargetedImps());
		}

		void setStats(CampaignStats s) {
			stats.setValues(s);
		}

		public AdxQuery[] getCampaignQueries() {
			return campaignQueries;
		}

		public void setCampaignQueries(AdxQuery[] campaignQueries) {
			this.campaignQueries = campaignQueries;
		}

	}

}
