package tau.tac.adx.demand;

import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;

import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.AdxManager;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class UserClassificationServiceImpl implements UserClassificationService {

	private final static double UCS_PROB = 0.9;

	private final Map<String, UserClassificationServiceAdNetData> advertisersData = new HashMap<String, UserClassificationServiceAdNetData>();
	private final Map<String, UserClassificationServiceAdNetData> tomorrowsAdvertisersData = new HashMap<String, UserClassificationServiceAdNetData>();

	@Override
	public void updateAdvertiserBid(String advertiser, double ucsBid, int day) {
		UserClassificationServiceAdNetData advData = tomorrowsAdvertisersData
				.get(advertiser);
		if (advData == null) {
			advData = new UserClassificationServiceAdNetData();
			advData.setAuctionResult(0,1.0,1);
			tomorrowsAdvertisersData.put(advertiser, advData);
		}
		advData.setBid(ucsBid, day);
	}

	@Override
	public UserClassificationServiceAdNetData getAdNetData(String advertiser) {
		return advertisersData.get(advertiser);
	}

	@Override
	public void auction(int day) {
		advertisersData.clear();
		advertisersData.putAll(tomorrowsAdvertisersData);
		int advCount = tomorrowsAdvertisersData.size();

		if (advCount > 0) {
			String[] advNames = new String[advCount + 1];
			double[] bids = new double[advCount + 1];
			int[] indices = new int[advCount + 1];

			int i = 0;

			for (String advName : tomorrowsAdvertisersData.keySet()) {
				advNames[i] = new String(advName);
				bids[i] = tomorrowsAdvertisersData.get(advName).bid;
				indices[i] = i;
				i++;
			}

			advNames[advCount] = "Zero";
			bids[advCount] = 0;
			indices[advCount] = advCount;

			hardSort(bids, indices);

			double ucsProb = 1.0;
			double levelPrice = 0;
			for (int j = 0; j < advCount; j++) {
				UserClassificationServiceAdNetData advData = tomorrowsAdvertisersData
						.get(advNames[indices[j]]);
				levelPrice = ucsProb * bids[indices[j]];
				advData.setAuctionResult(levelPrice, ucsProb, day + 1);
				AdxManager.getInstance().getSimulation()
						.broadcastUCSWin(advNames[j], levelPrice);
				ucsProb = ucsProb * UCS_PROB;
			}
		}
	}
	
	@Override
	public String logToString() {
		String ret = new String("");
		for (String adv : advertisersData.keySet()) {
			ret = ret + adv + advertisersData.get(adv).logToString();			
		}
		return ret;
	}
}
