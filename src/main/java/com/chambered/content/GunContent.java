package com.chambered.content;

import java.util.List;

/**
 * Authoritative content roster for guns + their magazines.
 * Data JSON / assets use the same ids; {@link com.chambered.registry.ModItems} registers from this list.
 */
public final class GunContent {
	public record Entry(
			String id,
			String displayName,
			String caliberId,
			String ammoTypeId,
			int magCapacity,
			String magDisplayName,
			Faction faction,
			Category category
	) {
		public String magId() {
			return id + "_magazine";
		}
	}

	public enum Faction {
		SOVIET,
		NATO
	}

	public enum Category {
		PISTOL,
		SMG,
		RIFLE,
		SNIPER,
		MACHINE_GUN,
		SHOTGUN
	}

	public static final List<Entry> ENTRIES = List.of(
			// Soviet / Warsaw Pact — pistols
			entry("makarov", "Makarov PM", "9x18", 8, "Makarov Magazine", Faction.SOVIET, Category.PISTOL),
			entry("stechkin_aps", "Stechkin APS", "9x18", 20, "Stechkin Magazine", Faction.SOVIET, Category.PISTOL),
			entry("tokarev_tt33", "Tokarev TT-33", "762x25", 8, "TT-33 Magazine", Faction.SOVIET, Category.PISTOL),
			// SMGs
			entry("pp19_bizon", "PP-19 Bizon", "9x18", 64, "Bizon Helical Magazine", Faction.SOVIET, Category.SMG),
			entry("aks74u", "AKS-74U", "545x39", 30, "AKS-74U Magazine", Faction.SOVIET, Category.SMG),
			// Rifles
			entry("mosin_nagant", "Mosin-Nagant", "762x54r", 5, "Mosin Clip", Faction.SOVIET, Category.RIFLE),
			entry("sks", "SKS", "762x39", 10, "SKS Stripper / Mag", Faction.SOVIET, Category.RIFLE),
			entry("ak74", "AK-74", "545x39", 30, "AK-74 Magazine", Faction.SOVIET, Category.RIFLE),
			entry("aks74", "AKS-74", "545x39", 30, "AKS-74 Magazine", Faction.SOVIET, Category.RIFLE),
			entry("akm", "AKM", "762x39", 30, "AKM Magazine", Faction.SOVIET, Category.RIFLE),
			entry("rpk", "RPK", "762x39", 40, "RPK Magazine", Faction.SOVIET, Category.RIFLE),
			entry("rpk74", "RPK-74", "545x39", 45, "RPK-74 Magazine", Faction.SOVIET, Category.RIFLE),
			entry("ots14_groza", "OTs-14 Groza", "545x39", 30, "Groza Magazine", Faction.SOVIET, Category.RIFLE),
			// Sniper / DMR
			entry("svd", "SVD Dragunov", "762x54r", 10, "SVD Magazine", Faction.SOVIET, Category.SNIPER),
			entry("vss_vintorez", "VSS Vintorez", "9x39", 10, "VSS Magazine", Faction.SOVIET, Category.SNIPER),
			entry("vsk94", "VSK-94", "9x39", 20, "VSK-94 Magazine", Faction.SOVIET, Category.SNIPER),
			// Machine guns
			entry("rpd", "RPD", "762x39", 100, "RPD Drum", Faction.SOVIET, Category.MACHINE_GUN),
			entry("pkm", "PKM", "762x54r", 100, "PKM Belt Box", Faction.SOVIET, Category.MACHINE_GUN),
			// Shotguns
			entry("saiga12", "Saiga-12", "12_gauge", 8, "Saiga-12 Magazine", Faction.SOVIET, Category.SHOTGUN),
			entry("izh43_sawn_off", "IZh-43 (Sawn-off)", "12_gauge", 2, "IZh-43 Shells", Faction.SOVIET, Category.SHOTGUN),
			entry("toz66_sawn_off", "TOZ-66 (Sawn-off)", "12_gauge", 2, "TOZ-66 Shells", Faction.SOVIET, Category.SHOTGUN),

			// NATO / UN — pistols
			entry("beretta_92fs", "Beretta 92FS / M9", "9x19", 15, "Beretta Magazine", Faction.NATO, Category.PISTOL),
			entry("sig_p226", "SIG Sauer P226", "9x19", 15, "P226 Magazine", Faction.NATO, Category.PISTOL),
			entry("colt_m1911a1", "Colt M1911A1", "45_acp", 7, "M1911 Magazine", Faction.NATO, Category.PISTOL),
			// SMGs
			entry("hk_mp5", "Heckler & Koch MP5", "9x19", 30, "MP5 Magazine", Faction.NATO, Category.SMG),
			entry("steyr_tmp", "Steyr TMP", "9x19", 30, "TMP Magazine", Faction.NATO, Category.SMG),
			entry("uzi", "Uzi", "9x19", 32, "Uzi Magazine", Faction.NATO, Category.SMG),
			// Rifles
			entry("m16a2", "Colt M16A2", "556x45", 30, "STANAG Magazine", Faction.NATO, Category.RIFLE),
			entry("m4_carbine", "M4 Carbine", "556x45", 30, "STANAG Magazine", Faction.NATO, Category.RIFLE),
			entry("hk_g3", "Heckler & Koch G3", "762x51", 20, "G3 Magazine", Faction.NATO, Category.RIFLE),
			entry("fn_fal", "FN FAL", "762x51", 20, "FAL Magazine", Faction.NATO, Category.RIFLE),
			entry("steyr_aug", "Steyr AUG", "556x45", 30, "AUG Magazine", Faction.NATO, Category.RIFLE),
			entry("m14", "M14", "762x51", 20, "M14 Magazine", Faction.NATO, Category.RIFLE),
			entry("famas", "FAMAS", "556x45", 25, "FAMAS Magazine", Faction.NATO, Category.RIFLE),
			// Sniper
			entry("remington_700", "Remington 700 / M24", "762x51", 5, "M24 Magazine", Faction.NATO, Category.SNIPER),
			entry("barrett_m82", "Barrett M82", "50_bmg", 10, "M82 Magazine", Faction.NATO, Category.SNIPER),
			// Machine guns
			entry("m240", "M240 (FN MAG)", "762x51", 100, "M240 Belt Box", Faction.NATO, Category.MACHINE_GUN),
			// Shotguns
			entry("spas12", "Franchi SPAS-12", "12_gauge", 8, "SPAS-12 Tube / Mag", Faction.NATO, Category.SHOTGUN),
			entry("remington_870", "Remington 870", "12_gauge", 7, "870 Tube", Faction.NATO, Category.SHOTGUN)
	);

	private GunContent() {
	}

	private static Entry entry(
			String id,
			String displayName,
			String caliberId,
			int magCapacity,
			String magDisplayName,
			Faction faction,
			Category category
	) {
		return new Entry(id, displayName, caliberId, caliberId + "_fmj", magCapacity, magDisplayName, faction, category);
	}
}
