public class PayrollRecord {
    private int payID;
    private String payDate;
    private double earnings;
    private double fedTax;
    private double fedMed;
    private double fedSS;
    private double stateTax;
    private double retire401k;
    private double healthCare;

    public PayrollRecord(int payID, String payDate, double earnings, double fedTax,
                         double fedMed, double fedSS, double stateTax,
                         double retire401k, double healthCare) {
        this.payID = payID;
        this.payDate = payDate;
        this.earnings = earnings;
        this.fedTax = fedTax;
        this.fedMed = fedMed;
        this.fedSS = fedSS;
        this.stateTax = stateTax;
        this.retire401k = retire401k;
        this.healthCare = healthCare;
    }

    public void printRecord() {
        System.out.printf(
                "%-6d %-12s %-10.2f %-10.2f %-10.2f %-10.2f %-10.2f %-10.2f %-10.2f%n",
                payID, payDate, earnings, fedTax, fedMed, fedSS, stateTax, retire401k, healthCare
        );
    }
}