package com.xue;

public class test {
    public static void main(String[] args) {
        String official_openid_boss = "oFTmu6Z3Wg2hiAXMe13yGsz35opY";
        String[] official_list = official_openid_boss.split(",");
        for(int k=0;k<official_list.length;k++){
            String official_openid_get = official_list[k];
            System.out.println(official_openid_get);
        }

    }
}
