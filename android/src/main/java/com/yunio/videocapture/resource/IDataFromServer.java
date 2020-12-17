package com.yunio.videocapture.resource;

/**
 * Created by PeterZhang on 2015/8/27.
 */
public interface IDataFromServer {
    /**
     * 从服务端取得数据并解析完成的回调方法(作为钩子方法使用)
     */
    public void onParseComplete();
}
