/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.core.arbitration;

import com.google.protobuf.ByteString;
import io.bisq.common.proto.ProtoUtil;
import io.bisq.common.proto.network.NetworkPayload;
import io.bisq.core.arbitration.messages.DisputeCommunicationMessage;
import io.bisq.generated.protobuffer.PB;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;

import java.util.Date;

@EqualsAndHashCode
@Getter
@ToString
@Slf4j
public final class DisputeResult implements NetworkPayload {

    public enum Winner {
        BUYER,
        SELLER
    }

    public enum Reason {
        OTHER,
        BUG,
        USABILITY,
        SCAM,
        PROTOCOL_VIOLATION,
        NO_REPLY,
        BANK_PROBLEMS
    }

    private final String tradeId;
    private final int traderId;
    private Winner winner;
    private int reasonOrdinal = Reason.OTHER.ordinal();
    private final BooleanProperty tamperProofEvidenceProperty = new SimpleBooleanProperty();
    private final BooleanProperty idVerificationProperty = new SimpleBooleanProperty();
    private final BooleanProperty screenCastProperty = new SimpleBooleanProperty();
    private final StringProperty summaryNotesProperty = new SimpleStringProperty();
    private DisputeCommunicationMessage disputeCommunicationMessage;
    private byte[] arbitratorSignature;
    private long buyerPayoutAmount;
    private long sellerPayoutAmount;
    private byte[] arbitratorPubKey;
    private long closeDate;
    private boolean isLoserPublisher;

    public DisputeResult(String tradeId, int traderId) {
        this.tradeId = tradeId;
        this.traderId = traderId;
    }

    public DisputeResult(String tradeId,
                         int traderId,
                         Winner winner,
                         int reasonOrdinal,
                         boolean tamperProofEvidence,
                         boolean idVerification,
                         boolean screenCast,
                         String summaryNotes,
                         DisputeCommunicationMessage disputeCommunicationMessage,
                         byte[] arbitratorSignature,
                         long buyerPayoutAmount,
                         long sellerPayoutAmount,
                         byte[] arbitratorPubKey,
                         long closeDate,
                         boolean isLoserPublisher) {
        this.tradeId = tradeId;
        this.traderId = traderId;
        this.winner = winner;
        this.reasonOrdinal = reasonOrdinal;
        this.tamperProofEvidenceProperty.set(tamperProofEvidence);
        this.idVerificationProperty.set(idVerification);
        this.screenCastProperty.set(screenCast);
        this.summaryNotesProperty.set(summaryNotes);
        this.disputeCommunicationMessage = disputeCommunicationMessage;
        this.arbitratorSignature = arbitratorSignature;
        this.buyerPayoutAmount = buyerPayoutAmount;
        this.sellerPayoutAmount = sellerPayoutAmount;
        this.arbitratorPubKey = arbitratorPubKey;
        this.closeDate = closeDate;
        this.isLoserPublisher = isLoserPublisher;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    public static DisputeResult fromProto(PB.DisputeResult proto) {
        return new DisputeResult(proto.getTradeId(),
                proto.getTraderId(),
                ProtoUtil.enumFromProto(DisputeResult.Winner.class, proto.getWinner().name()),
                proto.getReasonOrdinal(),
                proto.getTamperProofEvidence(),
                proto.getIdVerification(),
                proto.getScreenCast(),
                proto.getSummaryNotes(),
                DisputeCommunicationMessage.fromPayloadProto(proto.getDisputeCommunicationMessage()),
                proto.getArbitratorSignature().toByteArray(),
                proto.getBuyerPayoutAmount(),
                proto.getSellerPayoutAmount(),
                proto.getArbitratorPubKey().toByteArray(),
                proto.getCloseDate(),
                proto.getIsLoserPublisher());
    }

    @Override
    public PB.DisputeResult toProtoMessage() {
        return PB.DisputeResult.newBuilder()
                .setTradeId(tradeId)
                .setTraderId(traderId)
                .setWinner(PB.DisputeResult.Winner.valueOf(winner.name()))
                .setReasonOrdinal(reasonOrdinal)
                .setTamperProofEvidence(tamperProofEvidenceProperty.get())
                .setIdVerification(idVerificationProperty.get())
                .setScreenCast(screenCastProperty.get())
                .setSummaryNotes(summaryNotesProperty.get())
                .setDisputeCommunicationMessage(disputeCommunicationMessage.toProtoNetworkEnvelope().getDisputeCommunicationMessage())
                .setArbitratorSignature(ByteString.copyFrom(arbitratorSignature))
                .setBuyerPayoutAmount(buyerPayoutAmount)
                .setSellerPayoutAmount(sellerPayoutAmount)
                .setArbitratorPubKey(ByteString.copyFrom(arbitratorPubKey))
                .setCloseDate(closeDate)
                .setIsLoserPublisher(isLoserPublisher).build();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public BooleanProperty tamperProofEvidenceProperty() {
        return tamperProofEvidenceProperty;
    }

    public BooleanProperty idVerificationProperty() {
        return idVerificationProperty;
    }

    public BooleanProperty screenCastProperty() {
        return screenCastProperty;
    }

    public void setReason(Reason reason) {
        this.reasonOrdinal = reason.ordinal();
    }

    public Reason getReason() {
        if (reasonOrdinal < Reason.values().length)
            return Reason.values()[reasonOrdinal];
        else
            return Reason.OTHER;
    }

    public void setSummaryNotes(String summaryNotes) {
        this.summaryNotesProperty.set(summaryNotes);
    }

    public StringProperty summaryNotesProperty() {
        return summaryNotesProperty;
    }

    public void setDisputeCommunicationMessage(DisputeCommunicationMessage disputeCommunicationMessage) {
        this.disputeCommunicationMessage = disputeCommunicationMessage;
    }

    public DisputeCommunicationMessage getDisputeCommunicationMessage() {
        return disputeCommunicationMessage;
    }

    public void setArbitratorSignature(byte[] arbitratorSignature) {
        this.arbitratorSignature = arbitratorSignature;
    }

    public byte[] getArbitratorSignature() {
        return arbitratorSignature;
    }

    public void setBuyerPayoutAmount(Coin buyerPayoutAmount) {
        this.buyerPayoutAmount = buyerPayoutAmount.value;
    }

    public Coin getBuyerPayoutAmount() {
        return Coin.valueOf(buyerPayoutAmount);
    }

    public void setSellerPayoutAmount(Coin sellerPayoutAmount) {
        this.sellerPayoutAmount = sellerPayoutAmount.value;
    }

    public Coin getSellerPayoutAmount() {
        return Coin.valueOf(sellerPayoutAmount);
    }

    public void setArbitratorPubKey(byte[] arbitratorPubKey) {
        this.arbitratorPubKey = arbitratorPubKey;
    }

    public byte[] getArbitratorPubKey() {
        return arbitratorPubKey;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate.getTime();
    }

    public Date getCloseDate() {
        return new Date(closeDate);
    }

    public void setWinner(Winner winner) {
        this.winner = winner;
    }

    public Winner getWinner() {
        return winner;
    }

    public void setLoserIsPublisher(boolean loserPublisher) {
        this.isLoserPublisher = loserPublisher;
    }

    public boolean isLoserPublisher() {
        return isLoserPublisher;
    }
}
