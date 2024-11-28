package main.member;

public class AcceptedProposal {

    private String proposalNum; // proposal's unique ID
    private String proposalVal; // value associated with the proposal

    // default no proposal accepted
    public AcceptedProposal() {
        this.proposalNum = null;
        this.proposalVal = null;
    }

    // update acc prop with a new number and value
    public void setAcceptedProposal(String proposalNum, String proposalVal) {
        this.proposalNum = proposalNum;
        this.proposalVal = proposalVal;
    }

    // get current acc proposal
    public AcceptedProposal getAcceptedProposal() {
        return this;
    }

    // get proposal number
    public String getProposalNumber() {
        return this.proposalNum;
    }

    // get the proposal value
    public String getProposalValue() {
        return this.proposalVal;
    }

    // check if the proposal is valid - not null
    public boolean isNull() {
        return this.proposalNum != null && this.proposalVal != null;
    }
}
