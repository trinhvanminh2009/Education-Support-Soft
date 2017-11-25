package com.example.pc.edu_support_soft;

/**
 * Created by pc on 24/11/2017.
 */

public class Solution {
    private String whoDidIt;
    private String bitmapUrl;
    private int score;


    public Solution(String whoDidIt, String bitmapUrl, int score) {
        this.whoDidIt = whoDidIt;
        this.bitmapUrl = bitmapUrl;
        this.score = score;
    }

    public String getWhoDidIt() {
        return whoDidIt;
    }

    public void setWhoDidIt(String whoDidIt) {
        this.whoDidIt = whoDidIt;
    }

    public String getBitmapUrl() {
        return bitmapUrl;
    }

    public void setBitmapUrl(String bitmapUrl) {
        this.bitmapUrl = bitmapUrl;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
