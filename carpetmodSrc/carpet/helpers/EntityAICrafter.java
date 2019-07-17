/*
 * 
Copyright (c) <2018> <Xcom>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
  
 * Code made for carpet client by Xcom
 * 
 * This is an experimental version of the nitwit villager AI.
 * The aim is to make them craft items if they have the items in there inventory.
 * 
 * Villagers are supposed to have 3 different tiers of crafting recipes. The 2 higher
 * tiers are unlockable by having the nitwit craft the first tier or the 2nd when it
 * unlocks. They pickup items based on the crafting they are doing and they will swap
 * crafting jobs if they have nothing to do for a while. They will also consume 
 * food to craft and randomly have a preferred food type they craft faster with and
 * a dislike food they craft slower with. The longer they craft the faster they become.
 * 
 */

package carpet.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import carpet.utils.Messenger;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAICrafter extends EntityAIBase {

	private EntityVillager villager;
	private int currentTask;
	private int craftingCooldown;

	private int[] tasks = new int[3];
	private final int tier2Unlock = 1200 * 8;
	private final int tier3Unlock = 1200 * 64;
	private IRecipe[] taskList = new IRecipe[3];

	private final int foodSlot = 7;
	private int[] food = new int[3];
	private int foodSize;
	private float foodSpeed;
	private static Item[] foods = { Items.BREAD, Items.POTATO, Items.CARROT, Items.BEETROOT };

	private Random randy = new Random();
	private int cooldown;
	private int batchSize;
	private int foodCooldown;

	private int idleTimer;
	private boolean craftingCanHappen; // optimization for not having to check
										// full
										// crafting
	private boolean statsDone = false;

	private int researchCraftingTable;
	private BlockPos craftingTablePosition;
	private String villagerName;
	
	private boolean inishilized = false;

	private static String[] recipeList = { "yellow_wool", // 0
			"yellow_stained_hardened_clay", // 1
			"yellow_stained_glass_pane", // 2
			"yellow_stained_glass", // 3
			"yellow_dye_from_sunflower", // 4
			"yellow_dye_from_dandelion", // 5
			"yellow_concrete_powder", // 6
			"yellow_carpet", // 7
			"yellow_bed_from_white_bed", // 8
			"yellow_bed", // 9
			"yellow_banner", // 10
			"writable_book", // 11
			"wooden_sword", // 12
			"wooden_shovel", // 13
			"wooden_pressure_plate", // 14
			"wooden_pickaxe", // 15
			"wooden_hoe", // 16
			"wooden_door", // 17
			"wooden_button", // 18
			"wooden_axe", // 19
			"white_stained_hardened_clay", // 20
			"white_stained_glass_pane", // 21
			"white_stained_glass", // 22
			"white_concrete_powder", // 23
			"white_carpet", // 24
			"white_bed", // 25
			"white_banner", // 26
			"wheat", // 27
			"tripwire_hook", // 28
			"trapped_chest", // 29
			"trapdoor", // 30
			"torch", // 31
			"tnt_minecart", // 32
			"tnt", // 33
			"sugar", // 34
			"string_to_wool", // 35
			"stonebrick", // 36
			"stone_sword", // 37
			"stone_stairs", // 38
			"stone_slab", // 39
			"stone_shovel", // 40
			"stone_pressure_plate", // 41
			"stone_pickaxe", // 42
			"stone_hoe", // 43
			"stone_button", // 44
			"stone_brick_stairs", // 45
			"stone_brick_slab", // 46
			"stone_axe", // 47
			"sticky_piston", // 48
			"stick", // 49
			"spruce_wooden_slab", // 50
			"spruce_stairs", // 51
			"spruce_planks", // 52
			"spruce_fence_gate", // 53
			"spruce_fence", // 54
			"spruce_door", // 55
			"spruce_boat", // 56
			"spectral_arrow", // 57
			"speckled_melon", // 58
			"snow_layer", // 59
			"snow", // 60
			"smooth_sandstone", // 61
			"smooth_red_sandstone", // 62
			"slime_ball", // 63
			"slime", // 64
			"sign", // 65
			"shield", // 66
			"shears", // 67
			"sea_lantern", // 68
			"sandstone_stairs", // 69
			"sandstone_slab", // 70
			"sandstone", // 71
			"repeater", // 72
			"redstone_torch", // 73
			"redstone_lamp", // 74
			"redstone_block", // 75
			"redstone", // 76
			"red_wool", // 77
			"red_stained_hardened_clay", // 78
			"red_stained_glass_pane", // 79
			"red_stained_glass", // 80
			"red_sandstone_stairs", // 81
			"red_sandstone_slab", // 82
			"red_sandstone", // 83
			"red_nether_brick", // 84
			"red_dye_from_tulip", // 85
			"red_dye_from_rose_bush", // 86
			"red_dye_from_poppy", // 87
			"red_dye_from_beetroot", // 88
			"red_concrete_powder", // 89
			"red_carpet", // 90
			"red_bed_from_white_bed", // 91
			"red_bed", // 92
			"red_banner", // 93
			"rail", // 94
			"rabbit_stew_from_red_mushroom", // 95
			"rabbit_stew_from_brown_mushroom", // 96
			"quartz_stairs", // 97
			"quartz_slab", // 98
			"quartz_block", // 99
			"purpur_stairs", // 100
			"purpur_slab", // 101
			"purpur_pillar", // 102
			"purpur_block", // 103
			"purple_wool", // 104
			"purple_stained_hardened_clay", // 105
			"purple_stained_glass_pane", // 106
			"purple_stained_glass", // 107
			"purple_shulker_box", // 108
			"purple_dye", // 109
			"purple_concrete_powder", // 110
			"purple_carpet", // 111
			"purple_bed_from_white_bed", // 112
			"purple_bed", // 113
			"purple_banner", // 114
			"pumpkin_seeds", // 115
			"pumpkin_pie", // 116
			"prismarine_bricks", // 117
			"prismarine", // 118
			"polished_granite", // 119
			"polished_diorite", // 120
			"polished_andesite", // 121
			"piston", // 122
			"pink_wool", // 123
			"pink_stained_hardened_clay", // 124
			"pink_stained_glass_pane", // 125
			"pink_stained_glass", // 126
			"pink_dye_from_red_bonemeal", // 127
			"pink_dye_from_pink_tulip", // 128
			"pink_dye_from_peony", // 129
			"pink_concrete_powder", // 130
			"pink_carpet", // 131
			"pink_bed_from_white_bed", // 132
			"pink_bed", // 133
			"pink_banner", // 134
			"pillar_quartz_block", // 135
			"paper", // 136
			"painting", // 137
			"orange_wool", // 138
			"orange_stained_hardened_clay", // 139
			"orange_stained_glass_pane", // 140
			"orange_stained_glass", // 141
			"orange_dye_from_red_yellow", // 142
			"orange_dye_from_orange_tulip", // 143
			"orange_concrete_powder", // 144
			"orange_carpet", // 145
			"orange_bed_from_white_bed", // 146
			"orange_bed", // 147
			"orange_banner", // 148
			"observer", // 149
			"oak_wooden_slab", // 150
			"oak_stairs", // 151
			"oak_planks", // 152
			"noteblock", // 153
			"nether_wart_block", // 154
			"nether_brick_stairs", // 155
			"nether_brick_slab", // 156
			"nether_brick_fence", // 157
			"nether_brick", // 158
			"mushroom_stew", // 159
			"mossy_stonebrick", // 160
			"mossy_cobblestone_wall", // 161
			"mossy_cobblestone", // 162
			"minecart", // 163
			"melon_seeds", // 164
			"melon_block", // 165
			"map", // 166
			"magma_cream", // 167
			"magma", // 168
			"magenta_wool", // 169
			"magenta_stained_hardened_clay", // 170
			"magenta_stained_glass_pane", // 171
			"magenta_stained_glass", // 172
			"magenta_dye_from_purple_and_pink", // 173
			"magenta_dye_from_lilac", // 174
			"magenta_dye_from_lapis_red_pink", // 175
			"magenta_dye_from_lapis_ink_bonemeal", // 176
			"magenta_dye_from_allium", // 177
			"magenta_concrete_powder", // 178
			"magenta_carpet", // 179
			"magenta_bed_from_white_bed", // 180
			"magenta_bed", // 181
			"magenta_banner", // 182
			"lit_pumpkin", // 183
			"lime_wool", // 184
			"lime_stained_hardened_clay", // 185
			"lime_stained_glass_pane", // 186
			"lime_stained_glass", // 187
			"lime_dye", // 188
			"lime_concrete_powder", // 189
			"lime_carpet", // 190
			"lime_bed_from_white_bed", // 191
			"lime_bed", // 192
			"lime_banner", // 193
			"light_weighted_pressure_plate", // 194
			"light_gray_wool", // 195
			"light_gray_stained_hardened_clay", // 196
			"light_gray_stained_glass_pane", // 197
			"light_gray_stained_glass", // 198
			"light_gray_dye_from_white_tulip", // 199
			"light_gray_dye_from_oxeye_daisy", // 200
			"light_gray_dye_from_ink_bonemeal", // 201
			"light_gray_dye_from_gray_bonemeal", // 202
			"light_gray_dye_from_azure_bluet", // 203
			"light_gray_concrete_powder", // 204
			"light_gray_carpet", // 205
			"light_gray_bed_from_white_bed", // 206
			"light_gray_bed", // 207
			"light_gray_banner", // 208
			"light_blue_wool", // 209
			"light_blue_stained_hardened_clay", // 210
			"light_blue_stained_glass_pane", // 211
			"light_blue_stained_glass", // 212
			"light_blue_dye_from_lapis_bonemeal", // 213
			"light_blue_dye_from_blue_orchid", // 214
			"light_blue_concrete_powder", // 215
			"light_blue_carpet", // 216
			"light_blue_bed_from_white_bed", // 217
			"light_blue_bed", // 218
			"light_blue_banner", // 219
			"lever", // 220
			"leather_leggings", // 221
			"leather_helmet", // 222
			"leather_chestplate", // 223
			"leather_boots", // 224
			"leather", // 225
			"lead", // 226
			"lapis_lazuli", // 227
			"lapis_block", // 228
			"ladder", // 229
			"jungle_wooden_slab", // 230
			"jungle_stairs", // 231
			"jungle_planks", // 232
			"jungle_fence_gate", // 233
			"jungle_fence", // 234
			"jungle_door", // 235
			"jungle_boat", // 236
			"jukebox", // 237
			"item_frame", // 238
			"iron_trapdoor", // 239
			"iron_sword", // 240
			"iron_shovel", // 241
			"iron_pickaxe", // 242
			"iron_nugget", // 243
			"iron_leggings", // 244
			"iron_ingot_from_nuggets", // 245
			"iron_ingot_from_block", // 246
			"iron_hoe", // 247
			"iron_helmet", // 248
			"iron_door", // 249
			"iron_chestplate", // 250
			"iron_boots", // 251
			"iron_block", // 252
			"iron_bars", // 253
			"iron_axe", // 254
			"hopper_minecart", // 255
			"hopper", // 256
			"heavy_weighted_pressure_plate", // 257
			"hay_block", // 258
			"green_wool", // 259
			"green_stained_hardened_clay", // 260
			"green_stained_glass_pane", // 261
			"green_stained_glass", // 262
			"green_concrete_powder", // 263
			"green_carpet", // 264
			"green_bed_from_white_bed", // 265
			"green_bed", // 266
			"green_banner", // 267
			"gray_wool", // 268
			"gray_stained_hardened_clay", // 269
			"gray_stained_glass_pane", // 270
			"gray_stained_glass", // 271
			"gray_dye", // 272
			"gray_concrete_powder", // 273
			"gray_carpet", // 274
			"gray_bed_from_white_bed", // 275
			"gray_bed", // 276
			"gray_banner", // 277
			"granite", // 278
			"golden_sword", // 279
			"golden_shovel", // 280
			"golden_rail", // 281
			"golden_pickaxe", // 282
			"golden_leggings", // 283
			"golden_hoe", // 284
			"golden_helmet", // 285
			"golden_chestplate", // 286
			"golden_carrot", // 287
			"golden_boots", // 288
			"golden_axe", // 289
			"golden_apple", // 290
			"gold_nugget", // 291
			"gold_ingot_from_nuggets", // 292
			"gold_ingot_from_block", // 293
			"gold_block", // 294
			"glowstone", // 295
			"glass_pane", // 296
			"glass_bottle", // 297
			"furnace_minecart", // 298
			"furnace", // 299
			"flower_pot", // 300
			"flint_and_steel", // 301
			"fishing_rod", // 302
			"fire_charge", // 303
			"fermented_spider_eye", // 304
			"fence_gate", // 305
			"fence", // 306
			"ender_eye", // 307
			"ender_chest", // 308
			"end_rod", // 309
			"end_crystal", // 310
			"end_bricks", // 311
			"enchanting_table", // 312
			"emerald_block", // 313
			"emerald", // 314
			"dropper", // 315
			"dispenser", // 316
			"diorite", // 317
			"diamond_sword", // 318
			"diamond_shovel", // 319
			"diamond_pickaxe", // 320
			"diamond_leggings", // 321
			"diamond_hoe", // 322
			"diamond_helmet", // 323
			"diamond_chestplate", // 324
			"diamond_boots", // 325
			"diamond_block", // 326
			"diamond_axe", // 327
			"diamond", // 328
			"detector_rail", // 329
			"daylight_detector", // 330
			"dark_prismarine", // 331
			"dark_oak_wooden_slab", // 332
			"dark_oak_stairs", // 333
			"dark_oak_planks", // 334
			"dark_oak_fence_gate", // 335
			"dark_oak_fence", // 336
			"dark_oak_door", // 337
			"dark_oak_boat", // 338
			"cyan_wool", // 339
			"cyan_stained_hardened_clay", // 340
			"cyan_stained_glass_pane", // 341
			"cyan_stained_glass", // 342
			"cyan_dye", // 343
			"cyan_concrete_powder", // 344
			"cyan_carpet", // 345
			"cyan_bed_from_white_bed", // 346
			"cyan_bed", // 347
			"cyan_banner", // 348
			"crafting_table", // 349
			"cookie", // 350
			"compass", // 351
			"comparator", // 352
			"cobblestone_wall", // 353
			"cobblestone_slab", // 354
			"coarse_dirt", // 355
			"coal_block", // 356
			"coal", // 357
			"clock", // 358
			"clay", // 359
			"chiseled_stonebrick", // 360
			"chiseled_sandstone", // 361
			"chiseled_red_sandstone", // 362
			"chiseled_quartz_block", // 363
			"chest_minecart", // 364
			"chest", // 365
			"cauldron", // 366
			"carrot_on_a_stick", // 367
			"cake", // 368
			"bucket", // 369
			"brown_wool", // 370
			"brown_stained_hardened_clay", // 371
			"brown_stained_glass_pane", // 372
			"brown_stained_glass", // 373
			"brown_concrete_powder", // 374
			"brown_carpet", // 375
			"brown_bed_from_white_bed", // 376
			"brown_bed", // 377
			"brown_banner", // 378
			"brick_stairs", // 379
			"brick_slab", // 380
			"brick_block", // 381
			"brewing_stand", // 382
			"bread", // 383
			"bowl", // 384
			"bow", // 385
			"bookshelf", // 386
			"book", // 387
			"bone_meal_from_bone", // 388
			"bone_meal_from_block", // 389
			"bone_block", // 390
			"boat", // 391
			"blue_wool", // 392
			"blue_stained_hardened_clay", // 393
			"blue_stained_glass_pane", // 394
			"blue_stained_glass", // 395
			"blue_concrete_powder", // 396
			"blue_carpet", // 397
			"blue_bed_from_white_bed", // 398
			"blue_bed", // 399
			"blue_banner", // 400
			"blaze_powder", // 401
			"black_wool", // 402
			"black_stained_hardened_clay", // 403
			"black_stained_glass_pane", // 404
			"black_stained_glass", // 405
			"black_concrete_powder", // 406
			"black_carpet", // 407
			"black_bed_from_white_bed", // 408
			"black_bed", // 409
			"black_banner", // 410
			"birch_wooden_slab", // 411
			"birch_stairs", // 412
			"birch_planks", // 413
			"birch_fence_gate", // 414
			"birch_fence", // 415
			"birch_door", // 416
			"birch_boat", // 417
			"beetroot_soup", // 418
			"beacon", // 419
			"arrow", // 420
			"armor_stand", // 421
			"anvil", // 422
			"andesite", // 423
			"activator_rail", // 424
			"acacia_wooden_slab", // 425
			"acacia_stairs", // 426
			"acacia_planks", // 427
			"acacia_fence_gate", // 428
			"acacia_fence", // 429
			"acacia_door", // 430
			"acacia_boat" // 431
	};

	private static String[] tier1 = { "blaze_powder", // 401
			"bucket", // 369
			"fire_charge", // 303
			"glowstone", // 295
			"gold_nugget", // 291
			"hay_block", // 258
			"lever", // 220
			"lit_pumpkin", // 183
			"nether_brick", // 158
			"prismarine", // 118
			"red_sandstone", // 83
			"redstone_torch", // 73
			"sandstone", // 71
			"snow", // 60
			"stick", // 49
			"sticky_piston", // 48
			"stonebrick", // 36
			"string_to_wool", // 35
			"sugar", // 34
			"spruce_planks", // 52
			"oak_planks", // 152
			"jungle_planks", // 232
			"dark_oak_planks", // 334
			"birch_planks", // 413
			"acacia_planks", // 427
			"trapped_chest", // 29
			"iron_trapdoor", // 239
			"brick_block", // 381
			"magma_cream", // 167
			"purpur_block", // 103
			"end_bricks", // 311
			"coarse_dirt", // 355
			"magma", // 168
			"mossy_cobblestone", // 162
	};

	private static String[] tier2 = { "dark_prismarine", // 331
			"fence", // 306
			"fence_gate", // 305
			"furnace", // 299
			"gold_block", // 294
			"ladder", // 229
			"lapis_block", // 228
			"cobblestone_wall", // 353
			"melon_block", // 165
			"cobblestone_slab", // 354
			"minecart", // 163
			"diamond_block", // 326
			"iron_block", // 252
			"boat", // 391
			"bone_block", // 390
			"coal_block", // 356
			"cauldron", // 366
			"paper", // 136
			"quartz_slab", // 98
			"quartz_stairs", // 97
			"redstone_block", // 75
			"redstone_lamp", // 74
			"sandstone_slab", // 70
			"rail", // 94
			"sandstone_stairs", // 69
			"trapdoor", // 30
			"tripwire_hook", // 28
			"slime", // 64
			"sea_lantern", // 68
			"nether_wart_block", // 154
			"iron_ingot_from_block", // 246
			"redstone", // 76
			"prismarine_bricks", // 117
			"armor_stand", // 421
			"book", // 387
			"emerald_block", // 313
			"spectral_arrow", // 57
			"wooden_door", // 17
			"iron_door", // 249
	};

	private static String[] tier3 = { "beacon", // 419
			"brewing_stand", // 382
			"chest", // 365
			"detector_rail", // 329
			"dispenser", // 316
			"dropper", // 315
			"anvil", // 422
			"ender_chest", // 308
			"activator_rail", // 424
			"fermented_spider_eye", // 304
			"tnt_minecart", // 32
			"noteblock", // 153
			"item_frame", // 238
			"daylight_detector", // 330
			"bow", // 385
			"comparator", // 352
			"golden_apple", // 290
			"golden_carrot", // 287
			"golden_rail", // 281
			"hopper", // 256
			"observer", // 149
			"sign", // 65
			"piston", // 122
			"speckled_melon", // 58
			"tnt", // 33
			"repeater", // 72
			"end_crystal", // 310
			"purple_shulker_box", // 108
			"end_rod", // 309
			"painting", // 137
	};

	/**
	 * Basic constructor for the crafting AI task
	 * 
	 * @param theVillagerIn
	 *            the villager object.
	 */
	public EntityAICrafter(EntityVillager theVillagerIn) {
		this.villager = theVillagerIn;
	}
	
	/**
	 * Global update to set there stats.
	 */
	public void updateNitwit(){
		updateCareerID();
		setupFoodSpeed();
		calcCooldown();
		resetIdleTimer();
		setName();
		inishilized = true;
	}

	/**
	 * Used to update the crafting jobs and other settings of the crafter based
	 * on the saved data.
	 */
	private void updateCareerID() {
		if (villager.careerId == 0) {
			randomiseStats();
		} else {
			decodeVillager();
		}
		fixFoodInventory();
		statsDone = true;
	}

	/**
	 * Fixes the food that is stuck in the wrong slots of the villager.
	 */
	private void fixFoodInventory() {
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		ItemStack foodStack = villagerInventory.getStackInSlot(foodSlot);
		boolean dropWrongFoods = false;

		if (isFood(foodStack.getItem())) {
			dropWrongFoods = true;
		}

		for (int i = 0; i < villagerInventory.getSizeInventory() - 1; ++i) {
			ItemStack inventoryItem = villagerInventory.getStackInSlot(i);
			if (isFood(inventoryItem.getItem())) {
				if (dropWrongFoods) {
					dropItem(inventoryItem);
					villagerInventory.markDirty();
				} else {
					villagerInventory.setInventorySlotContents(foodSlot, inventoryItem.copy());
					inventoryItem.setCount(0);
					villagerInventory.markDirty();
					dropWrongFoods = true;
				}
			}
		}
	}

	/**
	 * Randomizes the stats of a newly created crafting villager.
	 */
	private void randomiseStats() {
		tasks[0] = randy.nextInt(tier1.length);
		tasks[1] = randy.nextInt(tier2.length);
		tasks[2] = randy.nextInt(tier3.length);

		food[0] = randy.nextInt(4);
		food[1] = randy.nextInt(4);
		while (food[0] == food[1]) {
			food[1] = randy.nextInt(4);
		}
		food[2] = 4 + randy.nextInt(4);

		foodSize = food[2];
		taskList[0] = getRecipe(tier1[tasks[0]]);
		taskList[1] = getRecipe(tier2[tasks[1]]);
		taskList[2] = getRecipe(tier3[tasks[2]]);
	}

	/**
	 * Decodes the stats from saved data and sets the stats of the villager.
	 */
	private void decodeVillager() {
		int taskEncoder = villager.careerId;
		int foodEncoder = villager.careerLevel;

		for (int i = 0; i < 3; i++) {
			tasks[i] = taskEncoder % 100;
			taskEncoder = taskEncoder / 100;
		}
		food[2] = taskEncoder % 100;
		taskEncoder = taskEncoder / 100;
		currentTask = taskEncoder % 10;

		for (int i = 0; i < 2; i++) {
			food[i] = (foodEncoder % 10) % 4;
			foodEncoder = foodEncoder / 10;
		}

		foodSize = food[2];
		try {
			taskList[0] = getRecipe(tier1[tasks[0]]);
			taskList[1] = getRecipe(tier2[tasks[1]]);
			taskList[2] = getRecipe(tier3[tasks[2]]);
		} catch (Exception e) {
			Messenger.print_server_message(villager.getServer(),
					"A villager with nasty craftings was found and stats was rerolled.");
			randomiseStats();
		}
	}

	/**
	 * Encodes the data for saving the villagers stats on disk.
	 */
	private void encodeVillager() {
		int taskEncoder = tasks[0] + tasks[1] * 100 + tasks[2] * 10000 + food[2] * 1000000 + currentTask * 100000000;
		int foodEncoder = food[0] + food[1] * 10;

		villager.careerId = taskEncoder;
		villager.careerLevel = foodEncoder;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		return true;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return this.currentTask >= 0 && super.shouldContinueExecuting();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		lookAtCraftingTables();

		if (craftingCooldown <= 0) {
			if (!craftingCanHappen) {
				if (idleTimer > 0) {
					idleTimer--;
				}
				return;
			}

			if (craftItems() && eatFood()) {
				calcCooldown();
				setName();
				craftingCooldown = cooldown;
				resetIdleTimer();
				encodeVillager();
			} else {
				craftingCanHappen = false;
			}
		} else if (craftingCooldown > 0) {
			if (eatFood()) {
				if (villager.wealth < 12000000) {
					villager.wealth++;
				}
				craftingCooldown--;
				encodeVillager();
			}
		}
	}

	/**
	 * Makes the villager turn towards crafting tables and updates the villagers
	 * crafting table if it exists.
	 */
	private void lookAtCraftingTables() {
		if (craftingTablePosition != null && researchCraftingTable > 0) {
			researchCraftingTable--;
			villager.getLookHelper().setLookPosition((double) craftingTablePosition.getX() + 0.5D,
					(double) (craftingTablePosition.getY() + 1.5), (double) craftingTablePosition.getZ() + 0.5D, 10.0F,
					(float) villager.getVerticalFaceSpeed());
		} else if (researchCraftingTable <= 0) {
			researchCraftingTable = 100;
			findCraftingTableNear();
		} else {
			researchCraftingTable--;
		}
	}

	/**
	 * Finds a crafting table in an area around the villager.
	 */
	private void findCraftingTableNear() {
		World worldIn = villager.getEntityWorld();
		BlockPos villagerpos = new BlockPos(villager);
		for (BlockPos pos : BlockPos.getAllInBox(villagerpos.add(-3, -1, -3), villagerpos.add(3, 4, 3))) {
			if (worldIn.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE) {
				craftingTablePosition = pos;
				craftingCanHappen = true;
				return;
			}
		}
		craftingTablePosition = null;
	}

	/**
	 * Sets the name of the villager for displaying what level they are at.
	 */
	private void setName() {
		String s = null;
		if (villager.wealth == 12000000) {
			s = "Grandmaster";
		} else if (villager.wealth > 8000000) {
			s = "Meister";
		} else if (villager.wealth > 4000000) {
			s = "Craftsman";
		} else if (villager.wealth > 2000000) {
			s = "Journeyman";
		} else if (villager.wealth > 1000000) {
			s = "Apprentice";
		} else if (villager.wealth > 500000) {
			s = "Novice";
		} else if (villager.wealth > tier3Unlock) {
			s = "Casual";
		} else if (villager.wealth > tier2Unlock) {
			s = "Nooblet";
		} else if (villager.wealth > 1000) {
			s = "Nitwit";
		}

		if (villagerName == null)
			villagerName = s;

		if (s != null && !villagerName.equals(s)) {
			villager.setCustomNameTag(s);
		}
	}

	/**
	 * Resets the idle timer that causes the villager to swap foods or jobs.
	 */
	private void resetIdleTimer() {
		idleTimer = 200 + randy.nextInt(100);
	}

	/**
	 * Sets the food speed based of the current food in the inventory on the
	 * food preference of the villager.
	 */
	private void setupFoodSpeed() {
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		ItemStack foodStack = getFoodStack(villagerInventory);
		setFoodSpeed(foodStack);
	}

	/**
	 * Recalculates the cooldowns and crafting amounts based on the experience
	 * of the villager.
	 */
	private void calcCooldown() {
		int welt = villager.wealth;
		if (welt < 0) {
			welt = 1;
		}
		float foodSpeed = foodPreference();
		cooldown = (int) ((107 / Math.pow(10, 0.0000001692d * welt) + 19) * foodSpeed);
		batchSize = (int) (welt / 1713000) + 1;
	}

	/**
	 * Returns the food preference speed.
	 * 
	 * @return returns the speed multiplier based on the food.
	 */
	private float foodPreference() {
		return foodSpeed;
	}

	/**
	 * Returns the recipe that the villager is currently crafting.
	 * 
	 * @return the recipe object that is being crafted currently.
	 */
	private IRecipe currentTaskRecipe() {
		if (currentTask < 0 || currentTask >= taskList.length) {
			currentTask = 0;
		}
		return taskList[currentTask];
	}

	/**
	 * Main crafting logic that performs the crafting based on the job
	 * 
	 * @return returns true if the crafting job is successful and false if not.
	 */
	private boolean craftItems() {
		if (!statsDone) {
			return false;
		}

		// tier 2 and above needs crafting table.
		if (craftingTablePosition == null && currentTask > 0) {
			return false;
		}

		InventoryBasic villagerInventory = villager.getVillagerInventory();
		ItemStack food = getFoodStack(villagerInventory);
		if (foodCooldown <= 0 && food.getCount() < foodSize) {
			return false;
		}

		boolean crafted = false;

		Map<ItemStack, Integer> map = genCraftingMap(currentTaskRecipe());

		if (map == null) {
			return false;
		}

		for (int batch = 0; batch < batchSize; batch++) {
			Map<ItemStack, Integer> crafting = findRelativeInventoryItemsForCrafting(map, villagerInventory);

			if (crafting != null) {
				for (Map.Entry<ItemStack, Integer> entry : crafting.entrySet()) {
					entry.getKey().setCount(entry.getKey().getCount() - entry.getValue());
				}
				dropItem(currentTaskRecipe().getCraftingResult(null));
				crafted = true;
			} else {
				break;
			}
		}

		if (crafted) {
			villagerInventory.markDirty();
		}

		return crafted;
	}

	/**
	 * Eats food if there is in the inventory and returns if the crafter is fed.
	 * 
	 * @return Returns true if the villager is fed. False if lacks food.
	 */
	private boolean eatFood() {
		if (foodCooldown > 0) {
			foodCooldown--;
			return true;
		}
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		ItemStack food = getFoodStack(villagerInventory);
		if (food.getCount() < foodSize) {
			return false;
		}

		food.setCount(food.getCount() - foodSize);
		foodCooldown = 160;
		return true;
	}

	/**
	 * The stack of food that is based on the last slot of the villager.
	 * 
	 * @param villagerInventory
	 *            The inventory object of the villager.
	 * @return Item stack of food based on the last slot.
	 */
	private ItemStack getFoodStack(InventoryBasic villagerInventory) {
		return villagerInventory.getStackInSlot(foodSlot);
	}

	/**
	 * Drops all items the villager is crafting except the food that is in the
	 * last slot. This is done to switch jobs.
	 */
	private void dropJob() {
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		for (int i = 0; i < villagerInventory.getSizeInventory() - 2; ++i) {
			ItemStack itemstack = villagerInventory.getStackInSlot(i);
			dropItem(itemstack.copy());
			itemstack.setCount(0);
		}
	}

	/**
	 * Find the relative inventory items of the currently selected job. A list
	 * of all inventory stacks and the amount that is needed to be deducted for
	 * the current job is returned.
	 * 
	 * @param list
	 *            A list of the items needed for the currently selected job.
	 * @param villagerInventory
	 *            Villager inventory object.
	 * @return List of item stacks and the amount needed to be deducted for the
	 *         currently selected job. Return null if the inventory items arent
	 *         sufficient for the recipe.
	 */
	private Map<ItemStack, Integer> findRelativeInventoryItemsForCrafting(Map<ItemStack, Integer> list,
			InventoryBasic villagerInventory) {
		Map<ItemStack, Integer> crafting = new HashMap<ItemStack, Integer>();
		Map<ItemStack, Integer> map = new HashMap<ItemStack, Integer>(list);

		for (int i = 0; i < villagerInventory.getSizeInventory() - 1; ++i) {
			for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
				ItemStack itemstack = villagerInventory.getStackInSlot(i);

				if (entry.getValue() > 0 && entry.getKey().getItem() == itemstack.getItem()) {
					int itemCount = map.get(entry.getKey());
					int invCount = itemstack.getCount();
					int reduce = Math.min(itemCount, invCount);
					int remains = itemCount - reduce;

					crafting.put(itemstack, reduce);

					map.put(entry.getKey(), remains);

					if (remains <= 0) {
						continue;
					}
				}
			}
		}

		for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
			if (entry.getValue() > 0) {
				return null;
			}
		}

		return crafting;
	}

	/**
	 * Creates a list of each item needed from crafting from the recipe object
	 * and the amount per item.
	 * 
	 * @param recipe
	 *            The recipe that is being used to create a list of items for.
	 * @return the list of items for the recipe and the amount per item.
	 */
	private Map<ItemStack, Integer> genCraftingMap(IRecipe recipe) {
		Map<ItemStack, Integer> map = new HashMap<ItemStack, Integer>();
		NonNullList<Ingredient> list = recipe.getIngredients();

		for (Ingredient ig : list) {
			ItemStack[] stack = ig.getMatchingStacks();
			if (stack.length > 0) {
				ItemStack is = stack[0];
				ItemStack is2 = itemIsInMap(map, is);
				if (is2 == null) {
					map.put(is, 1);
				} else {
					int i = map.get(is2);
					map.put(is2, i + 1);
				}
			}
		}

		return map;
	}

	/**
	 * Checks if the item stack type is in the list.
	 * 
	 * @param map
	 *            The list that is being checked for if it contains the item
	 *            stack type.
	 * @param itemstack
	 *            The item stack type that is being checked for.
	 * @return Returns true if the item stack type is in the list.
	 */
	private ItemStack itemIsInMap(Map<ItemStack, Integer> map, ItemStack itemstack) {
		for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
			if (entry.getKey().getItem() == itemstack.getItem())
				return entry.getKey();
		}
		return null;
	}

	/**
	 * Gets the amount of items of a specific item type found in the current job
	 * (active recipe)
	 * 
	 * @param item
	 *            The item type that is being checked for in the active recipe.
	 * @return The amount of items found in the active recipe.
	 */
	private int getActiveRecipeCount(Item item) {
		int itemCount = 0;
		NonNullList<Ingredient> list = currentTaskRecipe().getIngredients();
		for (Ingredient ig : list) {
			for (ItemStack is : ig.getMatchingStacks()) {
				if (is.getItem() == item) {
					itemCount++;
				}
			}
		}
		return itemCount;
	}

	/**
	 * Drops the items out of the villagers head towards the specified facing
	 * direction or towards a crafting table if the villager has chosen it as
	 * the active crafting table.
	 * 
	 * @param itemstack
	 *            The item stack that is being thrown out of the villager.
	 */
	private void dropItem(ItemStack itemstack) {
		if (itemstack.isEmpty())
			return;

		float f1 = villager.rotationYawHead;
		float f2 = villager.rotationPitch;

		if (craftingTablePosition != null) {
			double d0 = craftingTablePosition.getX() + 0.5D - villager.posX;
			double d1 = craftingTablePosition.getY() + 1.5D - (villager.posY + (double) villager.getEyeHeight());
			double d2 = craftingTablePosition.getZ() + 0.5D - villager.posZ;
			double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
			f1 = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
			f2 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
		}

		double d0 = villager.posY - 0.30000001192092896D + (double) villager.getEyeHeight();
		EntityItem entityitem = new EntityItem(villager.world, villager.posX, d0, villager.posZ, itemstack);
		float f = 0.3F;

		entityitem.motionX = (double) (-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f);
		entityitem.motionZ = (double) (MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f);
		entityitem.motionY = (double) (-MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F);
		entityitem.setDefaultPickupDelay();
		villager.world.spawnEntity(entityitem);
	}

	/**
	 * Updates the villager equipment based on the item type that is being
	 * picked up. If the item is a command block data is printed out, structure
	 * blocks delete all inventory items of the villager. If the villager can
	 * pickup the item the idle timer and the crafting is enabled. If the idle
	 * timer have kicked in the item is checked if it matches other recipes the
	 * villager have unlocked to switch jobs.
	 * 
	 * @param itemEntity
	 *            Item stack that is being picked up by the villager.
	 * @param villagerInventory
	 *            The villagers inventory object.
	 * @return Returns true if the villager should stop all other inventory
	 *         actions.
	 */
	public boolean updateEquipment(EntityItem itemEntity, InventoryBasic villagerInventory) {
		if(!inishilized){
			return false;
		}
		ItemStack itemstack = itemEntity.getItem();
		Item item = itemstack.getItem();
		if (itemstack.getItem().getTranslationKey().equals(Blocks.COMMAND_BLOCK.getTranslationKey())) {
			readoutDebugInfoOnMe();
			itemEntity.setDead();
			return true;
		} else if (itemstack.getItem().getTranslationKey().equals(Blocks.STRUCTURE_BLOCK.getTranslationKey())) {
			for (int i = 0; i < villagerInventory.getSizeInventory(); ++i) {
				villagerInventory.getStackInSlot(i).setCount(0);
			}
			itemEntity.setDead();
			return true;
		}

		if (!statsDone) {
			return true;
		}

		boolean foodCheck = isFood(item);
		ItemStack editedStack = null;

		if (foodCheck) {
			editedStack = addFood(itemstack, villagerInventory);
			processItems(itemEntity, itemstack, editedStack);
		} else if (craftingItemForPickup(item, currentTaskRecipe())) {
			editedStack = addRecipeItem(itemstack, villagerInventory);
			processItems(itemEntity, itemstack, editedStack);
		}

		boolean itemPickedUp = false;
		if (editedStack != null && editedStack.getCount() != itemstack.getCount()) {
			craftingCanHappen = true;
			itemPickedUp = true;
		}

		// Dirty place to put job switcher but what the hell
		// If ground items didn't get picked up and the idle timer is zero,
		// check for job switch.
		if (idleTimer <= 0 && !foodCheck && !itemPickedUp) {
			craftingCanHappen = true;
			checkJobSwitch(item);
		}

		return true;
	}

	/**
	 * Checks if the item type matches in the recipe lists of the other jobs the
	 * villager have unlocked and switches job if match is found.
	 * 
	 * @param item
	 *            The item type that is being used to check if it matches the
	 *            other jobs the villager have unlocked.
	 */
	private void checkJobSwitch(Item item) {
		int unlocked = getUnlockedLevel();

		if (unlocked <= 1) {
			return;
		} else {
			int newJob = randy.nextInt() % unlocked;
			while (currentTask == newJob || newJob < 0) {
				newJob = randy.nextInt() % unlocked;
			}
			if (craftingItemForPickup(item, taskList[newJob])) {
				switchJobTo(newJob);
			}
			idleTimer = 10;
		}
	}

	/**
	 * Switches the job and performs the dropping of items and resets the idle
	 * timer.
	 * 
	 * @param job
	 *            The new job that is being switched too.
	 */
	private void switchJobTo(int job) {
		currentTask = job;
		dropJob();
		resetIdleTimer();
	}

	/**
	 * Gets the unlocked levels based on the crafting experience of the
	 * villager.
	 * 
	 * @return Returns the level of what level the crafting villager have
	 *         unlocked based on the crafting experience.
	 */
	private int getUnlockedLevel() {
		int tier = 0;
		if (villager.wealth < tier2Unlock) {
			tier = 1;
		} else if (villager.wealth < tier3Unlock) {
			tier = 2;
		} else {
			tier = 3;
		}

		return tier;
	}

	/**
	 * Adds the food item into the villagers inventory. Placed in the last slot
	 * of the villager. If another food type is found it is swaped if the idle
	 * timer have kicked in (no crafting done in a period of 10-15 seconds).
	 * 
	 * @param stack
	 *            Item stack that is being placed into the villagers inventory.
	 * @param villagerInventory
	 *            The villagers inventory object.
	 * @return Returns the resulting change after attempting to place the item
	 *         stack into the villagers inventory. Unchanged if the item stack
	 *         can't be placed anywhere and returns empty if it was placed into
	 *         an empty slot or fully on top of another stack.
	 */
	private ItemStack addFood(ItemStack stack, InventoryBasic villagerInventory) {
		ItemStack groundItem = stack.copy();
		ItemStack inventoryItem = villagerInventory.getStackInSlot(foodSlot);

		if (inventoryItem.isEmpty()) {
			villagerInventory.setInventorySlotContents(foodSlot, groundItem);
			setFoodSpeed(groundItem);
			calcCooldown();
			resetIdleTimer();
			villagerInventory.markDirty();
			return ItemStack.EMPTY;
		} else {
			if (ItemStack.areItemsEqual(inventoryItem, groundItem)) {
				int j = Math.min(villagerInventory.getInventoryStackLimit(), inventoryItem.getMaxStackSize());
				int k = Math.min(groundItem.getCount(), j - inventoryItem.getCount());

				if (k > 0) {
					inventoryItem.grow(k);
					groundItem.shrink(k);

					if (groundItem.isEmpty()) {
						villagerInventory.markDirty();
						return ItemStack.EMPTY;
					}
				}
			} else if (idleTimer <= 0) {
				dropItem(inventoryItem);
				villagerInventory.setInventorySlotContents(foodSlot, groundItem);
				setFoodSpeed(groundItem);
				calcCooldown();
				resetIdleTimer();
				villagerInventory.markDirty();
				return ItemStack.EMPTY;
			}
		}

		if (groundItem.getCount() != stack.getCount()) {
			villagerInventory.markDirty();
		}

		return groundItem;
	}

	/**
	 * Sets the speed of the food based on the villagers food preference matched
	 * towards the item stack (food item stack).
	 * 
	 * @param itemstack
	 *            The item stack that is being used to check what food
	 *            preference the villager has.
	 */
	private void setFoodSpeed(ItemStack itemstack) {
		Item item = itemstack.getItem();
		int index = 0;

		for (Item im : foods) {
			if (im != item) {
				index++;
			} else {
				break;
			}
		}

		if (index == food[0]) {
			foodSpeed = 0.8f;
		} else if (index == food[1]) {
			foodSpeed = 1.2f;
		} else {
			foodSpeed = 1.0f;
		}
	}

	/**
	 * Adds the item into the villagers inventory. If the item is found it
	 * stacks the item. If the item is not found it places it into the first
	 * slot. Only one stack per item is used.
	 * 
	 * An exception is done on wooden planks where the wooden planks are stacked
	 * and the meta data of the plank is changed to the latest plank type that
	 * was stacked. This is done because of the villages limited inventory size
	 * and the large number of wooden planks in the game.
	 * 
	 * @param stack
	 *            Item stack that is being placed into the villagers inventory.
	 * @param villagerInventory
	 *            The villagers inventory object.
	 * @return Returns the resulting change after attempting to place the item
	 *         stack into the villagers inventory. Unchanged if the item stack
	 *         can't be placed anywhere and returns empty if it was placed into
	 *         an empty slot or fully on top of another stack.
	 */
	private ItemStack addRecipeItem(ItemStack stack, InventoryBasic villagerInventory) {
		ItemStack groundItem = stack.copy();
		ItemStack inventoryItem = null;
		int emptySlot = -1;
		boolean planks = plankCheck(groundItem);

		for (int i = 0; i < villagerInventory.getSizeInventory() - 1; ++i) {
			inventoryItem = villagerInventory.getStackInSlot(i);

			if (inventoryItem.isEmpty() && emptySlot == -1) {
				emptySlot = i;
				continue;
			}

			if (ItemStack.areItemsEqual(inventoryItem, groundItem)) {
				int j = Math.min(villagerInventory.getInventoryStackLimit(), inventoryItem.getMaxStackSize());
				int k = Math.min(groundItem.getCount(), j - inventoryItem.getCount());

				if (k > 0) {
					inventoryItem.grow(k);
					groundItem.shrink(k);

					if (groundItem.isEmpty()) {
						resetIdleTimer();
						villagerInventory.markDirty();
						return ItemStack.EMPTY;
					}
				}
				break;
			} else if (planks && !inventoryItem.isEmpty() && plankCheck(inventoryItem)) { // plankmerge
				int j = Math.min(villagerInventory.getInventoryStackLimit(), inventoryItem.getMaxStackSize());
				int k = Math.min(groundItem.getCount(), j - inventoryItem.getCount());

				if (k > 0) {
					inventoryItem.setItemDamage(groundItem.getItemDamage());
					inventoryItem.grow(k);
					groundItem.shrink(k);

					if (groundItem.isEmpty()) {
						resetIdleTimer();
						villagerInventory.markDirty();
						return ItemStack.EMPTY;
					}
				}
				break;
			}
		}

		if (emptySlot != -1) {
			resetIdleTimer();
			villagerInventory.setInventorySlotContents(emptySlot, groundItem);
			villagerInventory.markDirty();
			return ItemStack.EMPTY;
		}

		if (groundItem.getCount() != stack.getCount()) {
			resetIdleTimer();
			villagerInventory.markDirty();
		}

		return groundItem;
	}

	/**
	 * Checks if the item stack is a wooden plank of any type.
	 * 
	 * @param itemstack
	 *            Item stack that is being checked if its a plank or not.
	 * @return Returns true if the item stack is of the type plank.
	 */
	private boolean plankCheck(ItemStack itemstack) {
		return itemstack.getItem().getTranslationKey().equals(Blocks.PLANKS.getTranslationKey());
	}

	/**
	 * Used to stack items and delete the older item.
	 * 
	 * @param itemEntity
	 *            The item object that is being used to delete if needed.
	 * @param itemEntityStack
	 *            The stacking item stack that is being updated.
	 * @param itemEdited
	 *            The item stack that is used to stack on top of the stacking
	 *            item stack.
	 */
	private void processItems(EntityItem itemEntity, ItemStack itemEntityStack, ItemStack itemEdited) {
		if (itemEdited.isEmpty()) {
			itemEntity.setDead();
		} else {
			itemEntityStack.setCount(itemEdited.getCount());
		}
	}

	/**
	 * Checks if the specified Item type is in the recipe that is being checked
	 * towards.
	 * 
	 * @param item
	 *            The item type that is being checked if its in the recipe.
	 * @param irecipe
	 *            The recipe that is being checked if the item type is in.
	 * @return Returns true if the item type is in the recipe.
	 */
	private boolean craftingItemForPickup(Item item, IRecipe irecipe) {
		NonNullList<Ingredient> list = irecipe.getIngredients();
		for (Ingredient ig : list) {
			for (ItemStack is : ig.getMatchingStacks()) {
				if (is.getItem() == item) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the specific Item is a food that the villager can consume or
	 * not.
	 * 
	 * @param item
	 *            The item that is being checked if its a villager preferred food
	 *            or not.
	 * @return Returns true if its a food that can be consumed.
	 */
	private boolean isFood(Item item) {
		for (Item im : foods) {
			if (im == item)
				return true;
		}
		return false;
	}

	/**
	 * Total list of all crafting IRecpie's.
	 * 
	 * @return List of all IRecpie that can be crafted.
	 */
	private static List<IRecipe> recipeList() {
		return Lists.newArrayList(CraftingManager.REGISTRY);
	}

	/**
	 * IRecpie info from the crafting list found in vanilla code.
	 * 
	 * @param recipe
	 *            The string used to get the specific recipe.
	 * @return The specific IRecpie being request.
	 */
	private static IRecipe getRecipe(String recipe) {
		return CraftingManager.getRecipe(new ResourceLocation(recipe));
	}


	/**
	 * Drops the inventory of the killed villager except for blacklisted items that can have the chance to be used as id-converters.
	 */
	public void dropInventory(){
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		for (int j = 0; j < villagerInventory.getSizeInventory(); ++j) {
			ItemStack is = villagerInventory.getStackInSlot(j);
			boolean planks = plankCheck(is);
			boolean die = is.getItem() == Items.DYE;
			if(!planks && !die) {
				villager.entityDropItem(is, 0.0F);
			}
		}
	}

	/**
	 * Server printout of all the information related to this specific crafting
	 * villager.
	 */
	private void readoutDebugInfoOnMe() {
		InventoryBasic villagerInventory = villager.getVillagerInventory();
		StringBuilder sb = new StringBuilder();
		try{
			calcCooldown();
			sb.append("Crafter info:\n");
			sb.append("Craftings:\n");
			for (int i = 0; i < taskList.length; ++i) {
				IRecipe ir = taskList[i];
				sb.append("tier " + (i + 1) + ": " + ir.getRecipeOutput().getDisplayName() + "\n");
			}
			sb.append("Current Task: " + currentTaskRecipe().getRecipeOutput().getDisplayName() + "\n");
			sb.append("Crafting Cooldown(ticks)/Batch Size: " + cooldown + "/" + batchSize + "\n");
			sb.append("Food consumption per craft: " + food[2] + "\n");
			sb.append("Food preference: " + foods[food[0]].getItemStackDisplayName(new ItemStack(foods[food[0]]))
					+ "\nFood dislike: " + foods[food[1]].getItemStackDisplayName(new ItemStack(foods[food[1]])) + "\n");
			sb.append("Crafting experience: " + villager.wealth + "\n");
	
			sb.append("Inventory: \n");
			for (int j = 0; j < villagerInventory.getSizeInventory(); ++j) {
				sb.append("Slot: " + (j + 1) + ": " + villagerInventory.getStackInSlot(j).getDisplayName() + " : "
						+ villagerInventory.getStackInSlot(j).getCount() + "\n");
			}
		}catch(Exception e){}
		
		Messenger.print_server_message(villager.getServer(), sb.toString());
	}
}
