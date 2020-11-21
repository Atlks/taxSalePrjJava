DROP TABLE IF EXISTS "product商品表";
CREATE TABLE "product商品表" (
  "loc地点" TEXT,
  "item物品名" TEXT,
  "price价格" real,
  "qty数量" INTEGER,
  "item_total物品价格不含税" real,
  "itemtype物品类型" TEXT,
  "taxrate税率" real,
  "taxrate税率文本格式" TEXT,
  "item_tax物品税" real
);


-- ----------------------------
-- Table structure for prodType商品类型表
-- ----------------------------
DROP TABLE IF EXISTS "prodType商品类型表";
CREATE TABLE "prodType商品类型表" (
  "item物品名" TEXT,
  "type" TEXT
);

-- ----------------------------
-- Records of prodType商品类型表
-- ----------------------------
INSERT INTO "prodType商品类型表" VALUES ('potato chips', 'food');
INSERT INTO "prodType商品类型表" VALUES ('shirt', 'cloth');



-- ----------------------------
-- Table structure for Taxrate税率表
-- ----------------------------
DROP TABLE IF EXISTS "Taxrate税率表";
CREATE TABLE "Taxrate税率表" (
  "tax_rate_num税率数字格式" real,
  "tax rate税率" text,
  "loc地点" TEXT,
  "type" TEXT
);

-- ----------------------------
-- Records of Taxrate税率表
-- ----------------------------
INSERT INTO "Taxrate税率表" VALUES (0.0975, '9.75%', 'CA', 'other');
INSERT INTO "Taxrate税率表" VALUES (0.0, 0, 'CA', 'food');
INSERT INTO "Taxrate税率表" VALUES (0.0, 0, 'NY', 'food');
INSERT INTO "Taxrate税率表" VALUES (0.08875, '8.875%', 'NY', 'other');
INSERT INTO "Taxrate税率表" VALUES (0.0, 0, 'NY', 'cloth');