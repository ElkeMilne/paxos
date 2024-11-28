package main.member;

public class AcceptedProposal {

    private String proposalNumber; // proposal's unique ID
    private String proposalValue;  // value associated with the proposal

    // default no proposal accepted
    public AcceptedProposal() {
        this.proposalNumber = null;
        this.proposalValue = null;
    }

    // get current acc proposal
    public AcceptedProposal getAcceptedProposal() {
        return this;
    }

    // update acc prop with a new number and value.
    public void setAcceptedProposal(String proposalNumber, String proposalValue) {
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
    }

    // get proposal number
    public String getProposalNumber() {
        return this.proposalNumber;
    }

    // get the proposal value
    public String getProposalValue() {
        return this.proposalValue;
    }

    // check if the proposal is valid - not null
    public boolean isNull() {
        return this.proposalNumber != null && this.proposalValue != null;
    }
}
