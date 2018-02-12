import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;

    private boolean[] followees;

    private Set<Transaction> pendingTransactions;

    private Set<Integer> blackList;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.blackList = new HashSet<>(followees.length);
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        // revert candidates to a senderNode id set
        final Set<Integer> senders = candidates.stream().map(candidate -> candidate.sender).collect(toSet());
        for (int i = 0; i < this.followees.length; i++) {
            // if senderNode is my valid followee but not in candidate list, it is not a valid candidate
            if (this.followees[i] && !senders.contains(i))
                this.blackList.add(i);
        }
        this.pendingTransactions = candidates.stream()
                .filter(candidate -> !this.blackList.contains(candidate.sender))
                .map(candidate -> candidate.tx)  // generate pendingTransactions which belongs to candidate of my followees
                .collect(toSet());
    }
}
