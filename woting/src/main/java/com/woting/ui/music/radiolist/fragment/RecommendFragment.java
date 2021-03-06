package com.woting.ui.music.radiolist.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.woting.R;
import com.woting.common.config.GlobalConfig;
import com.woting.common.constant.BroadcastConstants;
import com.woting.common.constant.IntegerConstant;
import com.woting.common.constant.StringConstant;
import com.woting.common.util.DialogUtils;
import com.woting.common.util.PicassoBannerLoader;
import com.woting.common.util.ToastUtils;
import com.woting.common.volley.VolleyCallback;
import com.woting.common.volley.VolleyRequest;
import com.woting.common.widgetui.TipView;
import com.woting.common.widgetui.xlistview.XListView;
import com.woting.common.widgetui.xlistview.XListView.IXListViewListener;
import com.woting.ui.music.adapter.ContentAdapter;
import com.woting.ui.music.model.content;
import com.woting.ui.music.main.HomeActivity;
import com.woting.ui.musicplay.play.dao.SearchPlayerHistoryDao;
import com.woting.ui.musicplay.album.main.AlbumFragment;
import com.woting.ui.music.radiolist.adapter.ForNullAdapter;
import com.woting.ui.music.radiolist.main.RadioListFragment;
import com.woting.ui.music.radiolist.mode.Image;
import com.woting.ui.main.MainActivity;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类推荐列表
 *
 * @author woting11
 */
public class RecommendFragment extends Fragment implements TipView.WhiteViewClick {
    private Context context;
    private SearchPlayerHistoryDao dbDao;// 数据库
    private ContentAdapter adapter;
    private Banner mLoopViewPager;

    private List<Image> imageList=new ArrayList<>();
    private List<String> ImageStringList = new ArrayList<>();
    private List<content> newList = new ArrayList<>();

    private View rootView;
    private Dialog dialog;// 加载对话框
    private XListView mListView;// 列表
    private TipView tipView;// 没有网络、没有数据提示

    private boolean isFirst = true;
    private int page = 1;// 页码
    private int refreshType = 1;// refreshType == 1 为下拉加载  == 2 为上拉加载更多

    @Override
    public void onWhiteViewClick() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialog(context);
            sendRequest();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_radio_list_layout, container, false);

            mListView = (XListView) rootView.findViewById(R.id.listview_fm);
            tipView = (TipView) rootView.findViewById(R.id.tip_view);
            tipView.setWhiteClick(this);
            View headView = LayoutInflater.from(context).inflate(R.layout.headview_acitivity_radiolist, null);
            // 轮播图
            mLoopViewPager = (Banner) headView.findViewById(R.id.slideshowView);
            mListView.addHeaderView(headView);
            mLoopViewPager.setVisibility(View.GONE);
            setListener();
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {// 发送网络请求
                dialog = DialogUtils.Dialog(context);
                sendRequest();
                getImage();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        }
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
      /*  if (isVisibleToUser && adapter == null && getActivity() != null) {
  rh,          if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                if (!isFirst) dialog = DialogUtils.Dialogph(context, "正在获取数据");
                sendRequest();
                getImage();
                isFirst = false;
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        }*/
        // 如果轮播图没有的话重新加载轮播图
       /* if (imageList == null){
            getImage();
        }*/
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserVisibleHint(getUserVisibleHint());
    }

    // 请求网络数据
    public void sendRequest() {

            JSONObject jsonObject = VolleyRequest.getJsonObject(context);
            try {
                jsonObject.put("MediaType", "");
                jsonObject.put("CatalogType", RadioListFragment.catalogType);
                jsonObject.put("CatalogId", RadioListFragment.id);
                jsonObject.put("Page", String.valueOf(page));
                jsonObject.put("PerSize", "3");
                jsonObject.put("ResultType", "3");
                jsonObject.put("PageType", "0");
                jsonObject.put("PageSize", "10");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        VolleyRequest.requestPost(GlobalConfig.getContentUrl, RadioListFragment.tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (RadioListFragment.isCancel()) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        page++;
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        List<content> subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<content>>() {
                        }.getType());
                        if (refreshType == 1) newList.clear();
                        newList.addAll(subList);
                        if (adapter == null) {
                            mListView.setAdapter(adapter = new ContentAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setOnItem();
                        mListView.setPullLoadEnable(true);
                        tipView.setVisibility(View.GONE);
                    } else {
                        if(newList.size()>0){
                            if (adapter == null) {
                                mListView.setAdapter(adapter = new ContentAdapter(context, newList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        }else{
                            mListView.setAdapter(new ForNullAdapter(context));
                        }
                        if (refreshType == 1) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");
                        } else {
                            mListView.setPullLoadEnable(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(newList.size()>0){
                        if (adapter == null) {
                            mListView.setAdapter(adapter = new ContentAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }else{
                        mListView.setAdapter(new ForNullAdapter(context));
                    }
                    if (refreshType == 1) {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    } else{
                        mListView.setPullLoadEnable(false);
                    }
                }

                if (refreshType == 1) {
                    mListView.stopRefresh();
                } else {
                    mListView.stopLoadMore();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                if (refreshType == 1) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                } else {
                    ToastUtils.showVolleyError(context);
                }
            }
        });
    }

    private void setOnItem() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (newList != null && position >= 2) {
                    if (newList.get(position - 2) != null && newList.get(position - 2).getMediaType() != null) {
                        String MediaType = newList.get(position - 2).getMediaType();
                        if (MediaType.equals(StringConstant.TYPE_RADIO) || MediaType.equals(StringConstant.TYPE_AUDIO)) {

                            dbDao.savePlayerHistory(MediaType,newList,position-2);// 保存播放历史

                            Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                            Bundle bundle1 = new Bundle();
                            bundle1.putString(StringConstant.TEXT_CONTENT, newList.get(position - 2).getContentName());
                            push.putExtras(bundle1);
                            context.sendBroadcast(push);
                            MainActivity.change();
                        } else if (MediaType.equals(StringConstant.TYPE_SEQU)) {
                            AlbumFragment fragment = new AlbumFragment();
                            Bundle bundle = new Bundle();
                            bundle.putInt(StringConstant.FROM_TYPE, IntegerConstant.TAG_HOME);
                            bundle.putString("id", newList.get(position-2).getContentId());
                            fragment.setArguments(bundle);
                            HomeActivity.open(fragment);
                        } else {
                            ToastUtils.show_short(context, "暂不支持的Type类型");
                        }
                    }
                }
            }
        });
    }

    // 设置刷新、加载更多参数
    private void setListener() {
        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setXListViewListener(new IXListViewListener() {
            @Override
            public void onRefresh() {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 1;
                    page = 1;
                    sendRequest();
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_NET);
                }
            }

            @Override
            public void onLoadMore() {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 2;
                    sendRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
            }
        });
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    // 请求网络获取分类信息
    private void getImage() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "-1");
            jsonObject.put("CatalogId",RadioListFragment.id);
            jsonObject.put("Size", "-1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.requestPost(GlobalConfig.getImage, RadioListFragment.tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (RadioListFragment.isCancel()) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            imageList = new Gson().fromJson(result.getString("LoopImgs"), new TypeToken<List<Image>>() {
                            }.getType());
                            if (imageList != null && imageList.size() > 0) {
                                // 有轮播图
                                ImageStringList.clear();
                                mLoopViewPager.setImageLoader(new PicassoBannerLoader());
                                for (int i = 0; i < imageList.size(); i++) {
                                    ImageStringList.add(imageList.get(i).getLoopImg());
                                }
                                mLoopViewPager.setImages(ImageStringList);
                                mLoopViewPager.setOnBannerListener(new OnBannerListener() {
                                    @Override
                                    public void OnBannerClick(int position) {
                                        ToastUtils.show_always(context, ImageStringList.get(position));
                                    }
                                });
                                mLoopViewPager.start();
                                tipView.setVisibility(View.GONE);
                                mLoopViewPager.setVisibility(View.VISIBLE);
                            } else {
                                // 无轮播图，原先的轮播图就是隐藏的此处不需要操作
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mLoopViewPager.setVisibility(View.GONE);
                        }
                    } else {
                        mLoopViewPager.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mLoopViewPager.setVisibility(View.GONE);
                }

            }

            @Override
            protected void requestError(VolleyError error) {
                mLoopViewPager.setVisibility(View.GONE);
            }
        });
    }

//    public class PicassoImageLoader extends ImageLoader {
//        @Override
//        public void displayImage(Context context, Object path, ImageView imageView) {
//            /**
//             注意：
//             1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
//             2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
//             传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
//             切记不要胡乱强转！
//             */
//            String contentImg = path.toString();
//            if (!contentImg.startsWith("http")) {
//                contentImg = GlobalConfig.imageurl + contentImg;
//            }
//            contentImg = AssembleImageUrlUtils.assembleImageUrl150(contentImg);
//            Picasso.with(context).load(contentImg.replace("\\/", "/")).resize(50, 50).centerCrop().into(imageView);
//        }
//    }
}
