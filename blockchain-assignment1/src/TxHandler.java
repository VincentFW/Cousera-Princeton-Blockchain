import java.util.ArrayList;

public class TxHandler {

    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double SUM_OUT = 0;
        double SUM_IN = 0;
        ArrayList<UTXO> claimedUTXOs = new ArrayList<UTXO>();

        if(utxoPool == null)
            return false;

        for(int i=0; i<tx.getInputs().size();i++){
            Transaction.Input input = tx.getInput(i);

            UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
            //1.validate outputs are in UTXO pool
            if(!utxoPool.contains(utxo))
                return false;
            Transaction.Output out = utxoPool.getTxOutput(utxo);

            //2.verify the signature of input
            if(!Crypto.verifySignature(out.address,tx.getRawDataToSign(i),input.signature))
                return false;
            //3.no UTXO is claimed multiple times, compare with utxo.equals()
            if(claimedUTXOs.contains(utxo))
                return false;
            else
                claimedUTXOs.add(utxo);

            SUM_IN += out.value;
        }

        for(Transaction.Output output:tx.getOutputs()){

            //4.all of {@code tx}s output values are non-negative
            if(output.value < 0)
                return false;

            SUM_OUT += output.value;
        }

        //5.the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        return (SUM_IN >= SUM_OUT);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> validTxs = new ArrayList<>();
        //1.check tx correctness
        for(Transaction tx : possibleTxs){
            if(this.isValidTx(tx)){
                validTxs.add(tx);
                for(Transaction.Input input: tx.getInputs()){
                    //remove verified utxo, input has been spent
                    UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for(int i=0; i<tx.numOutputs();i++){
                    //add tx output as unspent tx
                    Transaction.Output output = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(),i);
                    utxoPool.addUTXO(utxo,output);
                }
            }

        }

        //3.return a valid array of accepted tx

        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

}
