package service;

import java.util.List;
import java.util.Map;
import com.rz.dao.Dao;
import com.rz.dao.Pager;
import com.rz.dao.SQLMapper;
import com.rz.tx.Transaction;

public class GoodsService
{
	private Dao dao = Dao.getInstance();
	
	public List<Map<String,Object>> category()
	{
		String sql = "select * from goods_category";
		return dao.find(sql);
	}
	
	public List<Map<String,Object>> unitlist()
	{
		String sql = "select * from goods_unit";
		return dao.find(sql);
	}

	public void unitadd(Map<String, String> map)
	{
		String sql = "insert into goods_unit(id,name) values(?,?)";
		int id = dao.getID("goodsunit");
		Object name = map.get("name");
		Object[] params = new Object[] { id, name };
		dao.update(sql, params);
	}

	@Transaction
	public void unitdel(String ids)
	{
		String[] arr = ids.split(",");
		StringBuffer sql1 = new StringBuffer("delete from goods_unit where id in (");
		for (int k = 0, len = arr.length; k < len; k++)
		{
			sql1.append("?");
			if (k != len - 1)
			{
				sql1.append(",");
			}
		}
		sql1.append(")");
		dao.update(sql1.toString(), arr);
		StringBuffer sql2 = new StringBuffer("delete from goods_unit where id in (");
		for (int k = 0, len = arr.length; k < len; k++)
		{
			sql2.append("?");
			if (k != len - 1)
			{
				sql2.append(",");
			}
		}
		sql2.append(")");
		dao.update(sql2.toString(), arr);
	}

	public void unitmod(Map<String, Object> map)
	{
		String sql = "update goods_unit set name=? where id=?";
		Object name = map.get("name");
		Object id = map.get("id");
		Object[] params = new Object[] { name, id };
		dao.update(sql, params);
	}

	public Pager list(Map<String, String> map)
	{
		String sqlid1 = "goods.count";
		String sqlid2 = "goods.selectAll";
		Pager pager = SQLMapper.pager(sqlid1, sqlid2, map);
		return pager;
	}

	public void add(Map<String, String> map)
	{
		String sql = "insert into goods(id,name,category,unit,spec,purchase_price,sale_price) values(?,?,?,?,?,?,?)";
		int id = dao.getID("goods");
		Object name = map.get("name");
		Object category = map.get("category");
		Object unit = map.get("unit");
		Object spec = map.get("spec");
		Object purchase_price = map.get("purchase_price");
		Object sale_price = map.get("sale_price");
		Object[] params = new Object[] { id, name, category, unit, spec, purchase_price, sale_price };
		dao.update(sql, params);
	}

	@Transaction
	public void del(String ids)
	{
		String[] arr = ids.split(",");
		StringBuffer sql1 = new StringBuffer("delete from goods where userid in (");
		for (int k = 0, len = arr.length; k < len; k++)
		{
			sql1.append("?");
			if (k != len - 1)
			{
				sql1.append(",");
			}
		}
		sql1.append(")");
		dao.update(sql1.toString(), arr);
		StringBuffer sql2 = new StringBuffer("delete from goods where id in (");
		for (int k = 0, len = arr.length; k < len; k++)
		{
			sql2.append("?");
			if (k != len - 1)
			{
				sql2.append(",");
			}
		}
		sql2.append(")");
		dao.update(sql2.toString(), arr);
	}

	public void mod(Map<String, Object> map)
	{
		String sql = "update goods set username=?,depart=?,memo=?,phone=?,email=?,gender=?,state=? where id=?";
		Object username = map.get("username");
		Object depart = map.get("depart");
		Object memo = map.get("memo");
		Object phone = map.get("phone");
		Object email = map.get("email");
		Object gender = map.get("gender");
		Object state = map.get("state");
		Object id = map.get("id");
		Object[] params = new Object[] { username, depart, memo, phone, email, gender, state, id };
		dao.update(sql, params);
	}
}