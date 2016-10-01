package com.grachro.mockshopapi;

import static spark.Spark.get;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class MockServer {

	private final static Logger logger = LoggerFactory.getLogger(MockServer.class);

	public static class Product {
		public final String name;
		public final int star = new Random().nextInt(6);// 0-5

		public Product(String name) {
			this.name = name;
		}
	}

	public static class CartItem {
		public final Product product;
		public int count = 0;

		public CartItem(Product product) {
			this.product = product;
		}
	}

	public static class Cart {
		private Map<String, CartItem> cartItems = new TreeMap<>();

		public synchronized Map<String, CartItem> getAll() {
			return this.cartItems;
		}

		public synchronized CartItem getOrCreate(String name) {
			CartItem item = this.cartItems.get(name);
			if (item == null) {
				Product p = new Product(name);
				item = new CartItem(p);
				this.cartItems.put(name, item);
			}
			return item;
		}

		public synchronized void add(String name) {
			CartItem item = this.getOrCreate(name);
			item.count++;
			logger.info("add " + name + " " + item.count);
		}

	}

	public static void main(String[] args) {

		Cart cart = new Cart();

		get("/", (request, response) -> {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("result", "ok");
			map.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

			return new Gson().toJson(map);
		});

		get("/allProducts", (request, response) -> {

			String[] allNames = new String[] { "apple", "orange", "banana", "tomato", "carrot", };
			List<Product> all = new ArrayList<>();

			for (String name : allNames) {
				Product p = new Product(name);
				all.add(p);
			}

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
			map.put("products", all);
			return new Gson().toJson(map);
		});

		get("/cart", (request, response) -> {
			return new Gson().toJson(cart);
		});

		get("/add/:name", (request, response) -> {
			String name = request.params(":name");
			cart.add(name);
			return new Gson().toJson(cart);
		});

	}
}