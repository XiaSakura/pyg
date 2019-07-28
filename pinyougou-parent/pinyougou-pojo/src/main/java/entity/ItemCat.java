package entity;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbItemCat;

public class ItemCat implements Serializable{
	private Long id;

    private Long parentId;

    private String name;

    private Long typeId;
    
    private List<TbItemCat> childrenCat;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Long getParentId() {
		return parentId;
	}


	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Long getTypeId() {
		return typeId;
	}


	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}


	public List<TbItemCat> getChildrenCat() {
		return childrenCat;
	}


	public void setChildrenCat(List<TbItemCat> childrenCat) {
		this.childrenCat = childrenCat;
	}
    
    
	
}	
