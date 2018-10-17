package com.cyl.musiclake.ui.music.discover

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.View
import com.cyl.musicapi.netease.BannerBean
import com.cyl.musiclake.R
import com.cyl.musiclake.base.BaseFragment
import com.cyl.musiclake.base.BaseLazyFragment
import com.cyl.musiclake.bean.Artist
import com.cyl.musiclake.bean.Playlist
import com.cyl.musiclake.common.Constants
import com.cyl.musiclake.common.NavigationHelper
import com.cyl.musiclake.ui.music.playlist.AllCategoryFragment
import kotlinx.android.synthetic.main.frag_discover.*


/**
 * 功能：在线排行榜
 * 作者：yonglong on 2016/8/11 18:14
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
class DiscoverFragment : BaseFragment<DiscoverPresenter>(), DiscoverContract.View, View.OnClickListener {

    private var mNeteaseAdapter: TopPlaylistAdapter? = null
    private var mArtistListAdapter: TopArtistListAdapter? = null
    private var mRadioAdapter: BaiduRadioAdapter? = null
    private var playlist = mutableListOf<Playlist>()
    private var artists = mutableListOf<Artist>()
    private var channels = mutableListOf<Playlist>()

    fun toCatTagAll() {
        AllCategoryFragment().apply {
            curCateName = this@DiscoverFragment.cateTagTv.text.toString()
            successListener = { result ->
                this@DiscoverFragment.updateCate(result)
            }
        }.showIt(mFragmentComponent.activity as FragmentActivity)
    }

    /**
     * 更新分类标签
     */
    private fun updateCate(name: String) {
        cateTagTv.text = name
        mPresenter?.loadNetease(name)
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_discover
    }

    override fun initViews() {

        wangChartsRv?.layoutManager = GridLayoutManager(activity, 2, LinearLayoutManager.VERTICAL, false)
        //适配器
        mNeteaseAdapter = TopPlaylistAdapter(playlist)
        wangChartsRv?.adapter = mNeteaseAdapter
        wangChartsRv?.isFocusable = false
        wangChartsRv?.isNestedScrollingEnabled = false
        mNeteaseAdapter?.bindToRecyclerView(wangChartsRv)

        chartsArtistRcv?.layoutManager = GridLayoutManager(activity, 2, LinearLayoutManager.HORIZONTAL, false)
        //适配器
        mArtistListAdapter = TopArtistListAdapter(artists)
        chartsArtistRcv?.adapter = mNeteaseAdapter
        chartsArtistRcv?.isFocusable = false
        chartsArtistRcv?.isNestedScrollingEnabled = false
        mArtistListAdapter?.bindToRecyclerView(chartsArtistRcv)

        //电台列表
        radioRsv?.layoutManager = GridLayoutManager(activity, 2, LinearLayoutManager.HORIZONTAL, false)
        //适配器
        mRadioAdapter = BaiduRadioAdapter(channels)
        radioRsv?.adapter = mRadioAdapter
        radioRsv?.isFocusable = false
        radioRsv?.isNestedScrollingEnabled = false
        mRadioAdapter?.bindToRecyclerView(radioRsv)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.singerListTv -> {
                activity?.let { NavigationHelper.navigateFragment(it, ArtistListFragment()) }
            }
            R.id.seeAllArtistTv, R.id.hotSingerTv -> {
                activity?.let { NavigationHelper.navigateFragment(it, AllListFragment.newInstance(Constants.NETEASE_ARITIST_LIST, artists, channels)) }
            }
            R.id.seeAllRadioTv, R.id.radioTv -> {
                activity?.let { NavigationHelper.navigateFragment(it, AllListFragment.newInstance(Constants.BAIDU_RADIO_LIST, artists, channels)) }
            }
            R.id.catTag1Tv -> {
                updateCate("华语")
            }
            R.id.catTag2Tv -> {
                updateCate("流行")
            }
            R.id.catTag3Tv -> {
                updateCate("古风")
            }
            R.id.cateTagTv -> toCatTagAll()
        }
    }

    override fun initInjector() {
        mFragmentComponent.inject(this)
    }

    override fun loadData() {
        showLoading()
        mPresenter?.loadNetease("全部")
        mPresenter?.loadArtists()
        mPresenter?.loadRaios()
    }

    override fun listener() {
        mNeteaseAdapter?.setOnItemClickListener { adapter, view, position ->
            val playlist = adapter.data[position] as Playlist
            NavigationHelper.navigateToPlaylist(mFragmentComponent.activity, playlist, Pair(view.findViewById(R.id.iv_cover), getString(R.string.transition_album)))
        }

        mArtistListAdapter?.setOnItemClickListener { adapter, view, position ->
            val artist = adapter.data[position] as Artist
            NavigationHelper.navigateToPlaylist(mFragmentComponent.activity, artist, Pair<View, String>(view.findViewById<View>(R.id.iv_cover), getString(R.string.transition_album)))
        }

        mRadioAdapter?.setOnItemClickListener { _, view, position ->
            NavigationHelper.navigateToPlaylist(mFragmentComponent.activity, channels[position], Pair(view.findViewById(R.id.iv_cover), getString(R.string.transition_album)))
        }

        catTag3Tv.setOnClickListener(this)
        cateTagTv.setOnClickListener(this)
        catTag1Tv.setOnClickListener(this)
        catTag2Tv.setOnClickListener(this)
        singerListTv.setOnClickListener(this)
        hotSingerTv.setOnClickListener(this)
        radioTv.setOnClickListener(this)
        seeAllRadioTv.setOnClickListener(this)
        seeAllArtistTv.setOnClickListener(this)
    }

//    override fun onLazyLoad() {
//    }

    override fun showEmptyView(msg: String) {
        hideLoading()
        showError(msg, true)
    }

    override fun retryLoading() {
        super.retryLoading()
        loadData()
    }

    override fun showBaiduCharts(charts: MutableList<Playlist>) {
    }

    override fun showBannerView(banners: MutableList<BannerBean>) {
//        for (i in 0 until banners.size) {
//            if (banners[i].targetType == "3000")
//                banners.removeAt(i)
//        }
        //banner
        mzBannerView.setPages(banners as List<Nothing>) { activity?.let { BannerViewHolder(it) } }
    }

    override fun onResume() {
        super.onResume()
        mzBannerView.start()
    }

    override fun onPause() {
        super.onPause()
        mzBannerView.pause()
    }

    override fun showNeteaseCharts(charts: MutableList<Playlist>) {
        hideLoading()
        playlistView.visibility = View.VISIBLE
        mNeteaseAdapter?.setNewData(charts)
    }

    override fun showArtistCharts(charts: MutableList<Artist>) {
        hideLoading()
        this.artists = charts
        mArtistListAdapter?.setNewData(charts)
    }

    override fun showRadioChannels(channels: MutableList<Playlist>) {
        hideLoading()
        this.channels = channels
        mRadioAdapter?.setNewData(channels)
    }

    companion object {
        fun newInstance(): DiscoverFragment {
            val args = Bundle()

            val fragment = DiscoverFragment()
            fragment.arguments = args
            return fragment
        }
    }

}