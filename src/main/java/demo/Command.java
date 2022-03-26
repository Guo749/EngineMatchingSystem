package demo;

/**
 * Used to indicate what command we do
 * for database
 */
public abstract class Command {
    /* how to execute this command in sql */
    public abstract String sqlCommand();
}

/**
 * Create Account Command
 */
class CreateAccount extends Command{
    /* which account to create */
    public final Account account;

    public CreateAccount(Account account){
        this.account = account;
    }

    @Override
    public String sqlCommand() {
        StringBuffer sb = new StringBuffer();


        return sb.toString();
    }
}

/**
 * Put Symbol Command
 */
class PutSymbol extends Command{
    /* which account to put the symbol */
    public final Account account;

    /* like SPY, BTC */
    public final String  symbol;

    /* number of share to be put in this account */
    public final double  share;

    public PutSymbol(Account account, String symbol, double share){
        this.account = account;
        this.symbol  = symbol;
        this.share   = share;
    }

    @Override
    public String sqlCommand() {
        StringBuffer sb = new StringBuffer();



        return sb.toString();
    }
}