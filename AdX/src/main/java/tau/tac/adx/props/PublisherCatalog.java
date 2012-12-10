/*
 * RetailCatalog.java
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
package tau.tac.adx.props;

import java.util.Iterator;

import tau.tac.adx.publishers.AdxPublisher;
import edu.umich.eecs.tac.props.AbstractTransportableEntryListBacking;
import edu.umich.eecs.tac.props.KeyIterator;

/**
 * A catalog of all available publishers.
 * 
 * @author greenwald
 */
public class PublisherCatalog extends
		AbstractTransportableEntryListBacking<AdxPublisher> implements
		Iterable<AdxPublisher> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5999861205883888430L;

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportableEntryListBacking#entryClass()
	 */
	@Override
	protected Class<AdxPublisher> entryClass() {
		return AdxPublisher.class;
	}

	/**
	 * Returns an iterator over the keys in the list.
	 * 
	 * @return an iterator over the keys in the list.
	 */
	@Override
	public final Iterator<AdxPublisher> iterator() {
		return new KeyIterator<AdxPublisher>(getEntries().iterator());
	}
}