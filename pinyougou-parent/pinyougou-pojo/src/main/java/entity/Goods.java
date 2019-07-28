package entity;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

/**
 * @author q9826 商品的封装类 包括商品的SPU和SKU 对三个表进行操作 goods goods_desc item
 */
public class Goods implements Serializable {

	private TbGoods goods; // 商品的SPU
	private TbGoodsDesc goodsDesc; // 商品的扩展信息
	private List<TbItem> itemList; // 商品的SKU列表
	public TbGoods getGoods() {
		return goods;
	}
	public void setGoods(TbGoods goods) {
		this.goods = goods;
	}
	public TbGoodsDesc getGoodsDesc() {
		return goodsDesc;
	}
	public void setGoodsDesc(TbGoodsDesc goodsDesc) {
		this.goodsDesc = goodsDesc;
	}
	public List<TbItem> getItemList() {
		return itemList;
	}
	public void setItemList(List<TbItem> itemList) {
		this.itemList = itemList;
	}
	

}
