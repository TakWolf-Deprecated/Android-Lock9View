package com.takwolf.android.lock9;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Lock9View extends ViewGroup {

    private int screenWidth; //屏幕尺寸
    private int itemWidth; //单个尺寸
    private int itemPadding; //单个内边距

    private List<Node> nodeList; //Point数组
    private DrawLineView drawLineView; //画线View

    public Lock9View(Context context) {
        super(context);
        init(context);
    }

    public Lock9View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //初始化尺寸参数
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        itemWidth = screenWidth / 3;
        itemPadding = itemWidth / 12;
        //初始化Point数组，3*3
        nodeList = new ArrayList<Node>();
        for (int n = 0; n < 9; n++) {
            //ImageView
            ImageView imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.lock_9_view_node_normal);
            addView(imageView);
            //行列和边界
            int row = n / 3; //行
            int col = n % 3; //列
            int left = col * itemWidth + itemPadding;
            int top = row * itemWidth + itemPadding; 
            int right = col * itemWidth + itemWidth - itemPadding;
            int bottom = row * itemWidth + itemWidth - itemPadding;
            Node point = new Node(left, right, top, bottom, imageView, n+1);
            nodeList.add(point);
        }
        // 初始化一个可以画线的view
        drawLineView = new DrawLineView(context, nodeList);
        addView(drawLineView);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (Node node : nodeList) {
            node.layout();
        }
        drawLineView.layout(l, t, r, b);
    }

}
