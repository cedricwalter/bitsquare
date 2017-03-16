/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.trade;

import io.bisq.app.Version;
import io.bisq.network_messages.NodeAddress;
import io.bisq.network_messages.protocol.trade.TradeMessage;
import io.bisq.storage.Storage;
import io.bisq.trade.offer.Offer;
import io.bisq.trade.protocol.trade.OffererProtocol;
import io.bisq.trade.protocol.trade.SellerAsOffererProtocol;
import org.bitcoinj.core.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;

public final class SellerAsOffererTrade extends SellerTrade implements OffererTrade {
    // That object is saved to disc. We need to take care of changes to not break deserialization.
    private static final long serialVersionUID = Version.LOCAL_DB_VERSION;

    private static final Logger log = LoggerFactory.getLogger(SellerAsOffererTrade.class);


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, initialization
    ///////////////////////////////////////////////////////////////////////////////////////////

    public SellerAsOffererTrade(Offer offer, Coin txFee, Coin takeOfferFee, Storage<? extends TradableList> storage) {
        super(offer, txFee, takeOfferFee, storage);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
            initStateProperties();
            initAmountProperty();
        } catch (Throwable t) {
            log.warn("Cannot be deserialized." + t.getMessage());
        }
    }

    @Override
    protected void createProtocol() {
        tradeProtocol = new SellerAsOffererProtocol(this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleTakeOfferRequest(TradeMessage message, NodeAddress taker) {
        ((OffererProtocol) tradeProtocol).handleTakeOfferRequest(message, taker);
    }
}