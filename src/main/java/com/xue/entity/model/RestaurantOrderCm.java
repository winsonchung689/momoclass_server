package com.xue.entity.model;


public class RestaurantOrderCm {

    private String id;
    private String order_no;
    private String openid;
    private String goods_id;
    private String create_time;
    private int delete_status;
    private String comment;
    private String uuids;


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public int getDelete_status() {
        return delete_status;
    }

    public void setDelete_status(int delete_status) {
        this.delete_status = delete_status;
    }

	@Override
	public String toString() {
		return "Order [goods_id="+ goods_id + " order_no="+ order_no + "]";
	}

    
}