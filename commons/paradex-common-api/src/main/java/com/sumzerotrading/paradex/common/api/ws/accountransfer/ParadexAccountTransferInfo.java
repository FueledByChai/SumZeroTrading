package com.sumzerotrading.paradex.common.api.ws.accountransfer;

public class ParadexAccountTransferInfo {

    public enum TransferType {
        DEPOSIT, WITHDRAWAL
    }

    public enum TransferStatus {
        PENDING, AVAILABLE, COMPLETED, FAILED
    }

    protected double amount;
    protected TransferType transferType;
    protected TransferStatus transferStatus;

    public ParadexAccountTransferInfo() {

    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public TransferStatus getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(TransferStatus transferStatus) {
        this.transferStatus = transferStatus;
    }

    @Override
    public String toString() {
        return "ParadexAccountTransferInfo [amount=" + amount + ", transferType=" + transferType + ", transferStatus="
                + transferStatus + "]";
    }

}
