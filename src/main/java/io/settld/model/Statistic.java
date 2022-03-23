package io.settld.model;

public class Statistic {

    private int numOfDots;
    private int numOfWords;
    private String mostUsedWord;
    //...Other filds


    public Statistic(int numOfDots, int numOfWords, String mostUsedWord) {
        this.numOfDots = numOfDots;
        this.numOfWords = numOfWords;
        this.mostUsedWord = mostUsedWord;
    }

    public int getNumOfDots() {
        return numOfDots;
    }

    public void setNumOfDots(int numOfDots) {
        this.numOfDots = numOfDots;
    }

    public int getNumOfWords() {
        return numOfWords;
    }

    public void setNumOfWords(int numOfWords) {
        this.numOfWords = numOfWords;
    }

    public String getMostUsedWord() {
        return mostUsedWord;
    }

    public void setMostUsedWord(String mostUsedWord) {
        this.mostUsedWord = mostUsedWord;
    }
}
