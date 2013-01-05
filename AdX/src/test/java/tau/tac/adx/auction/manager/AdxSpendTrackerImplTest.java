/*
 * AdxSpendTrackerImplTest.java
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
package tau.tac.adx.auction.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import tau.tac.adx.auction.tracker.AdxSpendTrackerImpl;
import tau.tac.adx.props.AdxQuery;

/**
 * @author Patrick Jordan
 */
public class AdxSpendTrackerImplTest {

	@Test
	public void testConstructor() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		assertNotNull(spendTracker);
	}

	@Test
	public void testAddAdvertiser() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		String advertiser = "Alice";

		assertEquals(spendTracker.size(), 0, 0);
		spendTracker.addAdvertiser(advertiser);
		assertEquals(spendTracker.size(), 1, 0);
		spendTracker.addAdvertiser(advertiser);
		assertEquals(spendTracker.size(), 1, 0);

		for (int i = 0; i < 8; i++) {
			spendTracker.addAdvertiser("" + i);
			assertEquals(spendTracker.size(), i + 2, 0);
		}
	}

	@Test
	public void testAddCost() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		String advertiser = "Alice";
		AdxQuery query = new AdxQuery();
		double cost = 1.0;

		spendTracker.addCost(advertiser, query, cost);

		assertEquals(spendTracker.getDailyCost(advertiser), cost, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, query), cost, 0.0);
		assertEquals(spendTracker.size(), 1, 0);

		spendTracker.addCost(advertiser, query, cost);

		assertEquals(spendTracker.getDailyCost(advertiser), 2 * cost, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, query), 2 * cost,
				0.0);
		assertEquals(spendTracker.size(), 1, 0);

		assertEquals(spendTracker.getDailyCost("notAlice"), 0.0, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, new AdxQuery("a", 1,
				null, null)), 0.0, 0.0);
		assertEquals(spendTracker.getDailyCost("notAlice", query), 0.0, 0.0);

		spendTracker.addAdvertiser("bob");
		assertEquals(spendTracker.getDailyCost("bob", query), 0.0, 0.0);

		spendTracker.addAdvertiser("bobbob");
		assertEquals(spendTracker.getDailyCost("bobbob"), 0.0, 0.0);

		spendTracker.reset();
		assertEquals(spendTracker.getDailyCost(advertiser), 0.0, 0.0);

		for (int i = 0; i < 8; i++) {
			spendTracker.addCost(advertiser, new AdxQuery(), cost);
		}
	}
}
