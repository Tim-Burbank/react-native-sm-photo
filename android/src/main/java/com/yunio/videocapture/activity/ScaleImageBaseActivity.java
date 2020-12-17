package com.yunio.videocapture.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.utils.ViewUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.MyViewPager;

import java.util.List;

public abstract class ScaleImageBaseActivity<T> extends BaseActivity
        implements OnPageChangeListener {

    protected final static String EXTRA_MEDIAS = "extra_media";
    protected final static String EXTRA_POSITION = "extra_position";
    private MyViewPager vpScaleImage;
    private TextView mTvTitle;
    protected List<T> medias;
    protected int position;
    protected int mImageSize = ViewUtils.getBigSize();
    protected ImagePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.hideStatusBarShowNavBar(this, 0x33000000, 0x33000000);
        setContentView(getLayoutResId());
        Intent intent = getIntent();
        medias = getMedias(intent);
        position = intent.getIntExtra(EXTRA_POSITION, 0);
        onInitUI();
    }

    protected void onInitUI() {
      FrameLayout layoutTitle = findViewById(R.id.title_bar);
      layoutTitle.setPadding(0, WindowUtils.getStatusBarHeight(this), 0, 0);
      mTvTitle = (TextView) findViewById(R.id.title_middle_text);
        vpScaleImage = (MyViewPager) findViewById(R.id.vp_scale_image);
        vpScaleImage.setPageMargin((int) getResources().getDimension(R.dimen.page_margin_small));
        if (mAdapter == null) {
            initAdapter();
        }
    }

    protected void initAdapter() {
        mAdapter = new ImagePagerAdapter();
        vpScaleImage.setAdapter(mAdapter);
        vpScaleImage.setCurrentItem(position);
        vpScaleImage.setOnPageChangeListener(this);
    }

    protected void setTitle() {
        int mPosition = position + 1;
        int total = medias.size();
        mTvTitle.setText(mPosition + "/" + total);
    }

    protected int getCurrentItem() {
        return vpScaleImage.getCurrentItem();
    }

    private class ImagePagerAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        public ImagePagerAdapter() {
            mInflater = LayoutInflater.from(ScaleImageBaseActivity.this);
        }

        @Override
        public int getCount() {
            return medias == null ? 0 : medias.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            try {
                if (object instanceof View) {
                    View view = (View) object;
                    container.removeView(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            T media = medias.get(position);
            View itemView = onCreateItemView(mInflater, media, position);
            updateItemView(media, itemView);
            container.addView(itemView, 0);
            return itemView;
        }
    }

    protected void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        setTitle();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    protected MyViewPager getViewPager() {
        return vpScaleImage;
    }

    protected abstract View onCreateItemView(LayoutInflater inflater, T media, int position);

    protected abstract int getLayoutResId();

    public abstract void updateItemView(T media, View itemView);

    public abstract List<T> getMedias(Intent intent);
}
