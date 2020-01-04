package com.fqxd.gftools;

class LSDTableClass {
    String GetLSDTable(int H,int M){
        String HHMM = null;
        switch (H){

            case 0:
                HHMM = case0(M);
                break;

            case 1:
                HHMM = case1(M);
                break;

            case 2:
                HHMM = case2(M);
                break;

            case 3:
                HHMM = case3(M);
                break;

            case 4:
                HHMM = case4(M);
                break;

            case 5:
                HHMM = case5(M);
                break;

            case 6:
                HHMM = case6(M);
                break;

            case 7:
                HHMM = case7(M);
                break;

            case 8:
                HHMM = case8(M);
                break;

            case 9:
                HHMM = case9(M);
                break;

            case 10:
                HHMM = case10(M);
                break;

            case 11:
                HHMM = case11(M);
                break;

            case 12:
                HHMM = case12(M);
                break;

            default:
                throwExcept(H,M);
                break;
        }
        return HHMM;
    }
    
    private void throwExcept(int H,int M){
        throw new IllegalArgumentException("Local " + H + "-" + M + " is not correct local!");
    }
    
    private String case0(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 0:
                imsi = "0001";
                break;

            case 1:
                imsi = "0050";
                break;

            case 2:
                imsi = "0300";
            break;

            case 3:
                imsi = "1200";
            break;

            case 4:
                imsi = "2400";
            break;
                
            default:
                throwExcept(0,M);
                break;
        }
        return imsi;
    }

    private String case1(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0015";
            break;

            case 2:
                imsi = "0030";
            break;

            case 3:
                imsi = "0100";
            break;

            case 4:
                imsi = "0200";
            break;

            default:
                throwExcept(1,M);
                break;
        }
        return imsi;
    }

    private String case2(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0040";
            break;

            case 2:
                imsi = "0130";
            break;

            case 3:
                imsi = "0400";
            break;

            case 4:
                imsi = "0600";
            break;

            default:
                throwExcept(2,M);
                break;
        }
        return imsi;
    }

    private String case3(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0020";
            break;

            case 2:
                imsi = "0045";
            break;

            case 3:
                imsi = "0130";
            break;

            case 4:
                imsi = "0500";
            break;

            default:
                throwExcept(3,M);
                break;
        }
        return imsi;
    }

    private String case4(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0100";
            break;

            case 2:
                imsi = "0200";
            break;

            case 3:
                imsi = "0600";
            break;

            case 4:
                imsi = "0800";
            break;

            default:
                throwExcept(4,M);
                break;
        }
        return imsi;
    }

    private String case5(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0030";
            break;

            case 2:
                imsi = "0230";
            break;

            case 3:
                imsi = "0400";
            break;

            case 4:
                imsi = "0700";
            break;

            default:
                throwExcept(5,M);
                break;
        }
        return imsi;
    }

    private String case6(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0200";
            break;

            case 2:
                imsi = "0300";
            break;

            case 3:
                imsi = "0500";
            break;

            case 4:
                imsi = "1200";
            break;

            default:
                throwExcept(6,M);
                break;
        }
        return imsi;
    }

    private String case7(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0230";
            break;

            case 2:
                imsi = "0400";
            break;

            case 3:
                imsi = "0530";
            break;

            case 4:
                imsi = "0800";
            break;

            default:
                throwExcept(7,M);
                break;
        }
        return imsi;
    }

    private String case8(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0100";
            break;

            case 2:
                imsi = "0300";
            break;

            case 3:
                imsi = "0600";
            break;

            case 4:
                imsi = "0900";
            break;

            default:
                throwExcept(8,M);
                break;
        }
        return imsi;
    }

    private String case9(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0030";
            break;

            case 2:
                imsi = "0130";
            break;

            case 3:
                imsi = "0430";
            break;

            case 4:
                imsi = "0700";
            break;

            default:
                throwExcept(9,M);
                break;
        }
        return imsi;
    }

    private String case10(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0040";
            break;

            case 2:
                imsi = "0140";
            break;

            case 3:
                imsi = "0520";
            break;

            case 4:
                imsi = "1000";
            break;

            default:
                throwExcept(10,M);
                break;
        }
        return imsi;
    }

    private String case11(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0400";
            break;

            case 2:
                imsi = "0400";
            break;

            case 3:
                imsi = "0800";
            break;

            case 4:
                imsi = "1000";
            break;

            default:
                throwExcept(11,M);
                break;
        }
        return imsi;
    }

    private String case12(int M)
    {
        String imsi = null;
        switch (M)
        {
            case 1:
                imsi = "0100";
                break;

            case 2:
                imsi = "0130";
                break;

            case 3:
                imsi = "0900";
                break;

            case 4:
                imsi = "1200";
                break;

            default:
                throwExcept(12,M);
                break;
        }
        return imsi;
    }
}
