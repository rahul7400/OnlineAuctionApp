package in.macrocodes.onlineauctionapp.Models;

public class BiddingModal {
    String bid,uid;

    public BiddingModal(){

    }
    public BiddingModal(String bid, String uid) {
        this.bid = bid;
        this.uid = uid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
