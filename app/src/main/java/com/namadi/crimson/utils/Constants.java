package com.namadi.crimson.utils;

public class Constants {
    public static final String MAINNET_CHANNEL = "mainnet-channel";
    public static final String ROPSTEN_CHANNEL = "ropsten-channel";
    public static final String RINKEBY_CHANNEL = "rinkeby-channel";

    public static final String CURRENT_CHANNEL = "current-channel";
    public static final String SCANNED_TO_SEND_ADDRESS = "scanned-to-send-address";
    public static final String SCANNED_TO_WATCH_ADDRESS = "scanned-to-watch-address";
    public static final Long WEI2ETH = 1000000000000000000L;

    //infura
    public static final String MAINNET_URL = "https://mainnet.infura.io/itRXy2X4KtzI7YZTq9wL";
    public static final String ROPSTEN_URL = "https://ropsten.infura.io/itRXy2X4KtzI7YZTq9wL";
    public static final String RINKEBY_URL = "https://rinkeby.infura.io/itRXy2X4KtzI7YZTq9wL";
    public static final String KOVAN_URL = "https://kovan.infura.io/itRXy2X4KtzI7YZTq9wL";
    public static final String GENERATE_WALLET = "https://pure-chamber-22089.herokuapp.com/com.namadi.crimson.wallet/";

    //https://api.tokenbalance.com/token/TOKEN_ADDRESS/ETH_ADDRESS
    public static final String MAINNET_TOKEN_BALANCE = "https://api.tokenbalance.com/token/%s/%s";
    public static final String ROPSTEN_TOKEN_BALANCE = "https://test.tokenbalance.com/token/%s/%s";
    public static final String RINKEBY_TOKEN_BALANCE = "https://rinkeby.tokenbalance.com/token/%s/%s";
    public static final String ROPSTEN_REQUEST_ETH = "https://faucet.ropsten.be/donate/%S";

    public static final String getChannelURL(String key) {
        switch (key) {
            case ROPSTEN_CHANNEL: return ROPSTEN_URL;
            case RINKEBY_CHANNEL: return RINKEBY_URL;
            default: return MAINNET_URL;
        }
    }
}