// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.Optional;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private ArrayList<Block> blockchain;
    private Block genesisBlock;
    private ArrayList<Transaction> transactions;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        this.blockchain = new ArrayList<Block>();
        this.genesisBlock = genesisBlock;
        this.blockchain.add(genesisBlock);
        this.transactions = new ArrayList<Transaction>();

    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        if (blockchain.size() <= 1)
            return this.genesisBlock;
        return blockchain.get(blockchain.size() - 1);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        Block block = this.getMaxHeightBlock();
        ArrayList<Transaction> transactions = block.getTransactions();
        UTXOPool utxoPool = new UTXOPool();
        // add coinbase tx into the pool
        transactions.add(block.getCoinbase());
        for(Transaction tx : transactions){
            for(int i=0; i<tx.getOutputs().size();i++){
                UTXO utxo = new UTXO(tx.getHash(),i);
                utxoPool.addUTXO(utxo,tx.getOutput(i));
            }
        }
        return utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        TransactionPool txpool = new TransactionPool();
        this.transactions.forEach(txpool::addTransaction);
        return txpool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        if(block.getPrevBlockHash() == null)
            return false;
        int parentHeight = findParentIndexByHash(block.getPrevBlockHash());
        if(parentHeight == -1 || (parentHeight + 1) <= (blockchain.size()-CUT_OFF_AGE))
            return false;

        TxHandler txHandler = new TxHandler(getMaxHeightUTXOPool());
        for(Transaction tx : block.getTransactions()){
           if(!txHandler.isValidTx(tx))
               return false;
        }
        blockchain.add(block);
        this.transactions.clear();
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.transactions.add(tx);
    }

    private int findParentIndexByHash(byte[] prevBlockHash){
        Optional<Block> parent = blockchain.stream().filter(t-> t.getHash() == prevBlockHash).findFirst();
        if(parent.isPresent())
            return blockchain.indexOf(parent.get());
        else
            return -1;
    }
}