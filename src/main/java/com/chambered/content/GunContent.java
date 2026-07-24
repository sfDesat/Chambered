package com.chambered.content;

import java.util.List;

/**
 * Authoritative content roster for guns.
 * Magazines are registered separately in {@link MagContent}; each gun lists which mag ids it accepts
 * (shared families + asymmetric accepts like RPK taking AKM mags).
 */
public final class GunContent {
	public record Entry(
			String id,
			String displayName,
			String caliberId,
			List<String> acceptedMagazines,
			Faction faction,
			Category category
	) {
		public String ammoTypeId() {
			return caliberId + "_fmj";
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
			gun("makarov", "Makarov PM", "9x18", List.of("makarov_magazine"), Faction.SOVIET, Category.PISTOL),
			gun("stechkin_aps", "Stechkin APS", "9x18", List.of("stechkin_aps_magazine"), Faction.SOVIET, Category.PISTOL),
			gun("tokarev_tt33", "Tokarev TT-33", "762x25", List.of("tokarev_tt33_magazine"), Faction.SOVIET, Category.PISTOL),
			// SMGs
			gun("pp19_bizon", "PP-19 Bizon", "9x18", List.of("pp19_bizon_magazine"), Faction.SOVIET, Category.SMG),
			gun("aks74u", "AKS-74U", "545x39", List.of("ak74_magazine"), Faction.SOVIET, Category.SMG),
			// Rifles
			gun("mosin_nagant", "Mosin-Nagant", "762x54r", List.of("mosin_nagant_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("sks", "SKS", "762x39", List.of("sks_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("ak74", "AK-74", "545x39", List.of("ak74_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("aks74", "AKS-74", "545x39", List.of("ak74_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("akm", "AKM", "762x39", List.of("akm_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("rpk", "RPK", "762x39", List.of("akm_magazine", "rpk_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("rpk74", "RPK-74", "545x39", List.of("ak74_magazine", "rpk74_magazine"), Faction.SOVIET, Category.RIFLE),
			gun("ots14_groza", "OTs-14 Groza", "545x39", List.of("ak74_magazine"), Faction.SOVIET, Category.RIFLE),
			// Sniper / DMR
			gun("svd", "SVD Dragunov", "762x54r", List.of("svd_magazine"), Faction.SOVIET, Category.SNIPER),
			gun("vss_vintorez", "VSS Vintorez", "9x39", List.of("vss_vintorez_magazine", "vsk94_magazine"), Faction.SOVIET, Category.SNIPER),
			gun("vsk94", "VSK-94", "9x39", List.of("vss_vintorez_magazine", "vsk94_magazine"), Faction.SOVIET, Category.SNIPER),
			// Machine guns
			gun("rpd", "RPD", "762x39", List.of("rpd_magazine"), Faction.SOVIET, Category.MACHINE_GUN),
			gun("pkm", "PKM", "762x54r", List.of("pkm_magazine"), Faction.SOVIET, Category.MACHINE_GUN),
			// Shotguns
			gun("saiga12", "Saiga-12", "12_gauge", List.of("saiga12_magazine"), Faction.SOVIET, Category.SHOTGUN),
			gun("izh43_sawn_off", "IZh-43 (Sawn-off)", "12_gauge", List.of("izh43_sawn_off_magazine"), Faction.SOVIET, Category.SHOTGUN),
			gun("toz66_sawn_off", "TOZ-66 (Sawn-off)", "12_gauge", List.of("toz66_sawn_off_magazine"), Faction.SOVIET, Category.SHOTGUN),

			// NATO / UN — pistols
			gun("beretta_92fs", "Beretta 92FS / M9", "9x19", List.of("beretta_92fs_magazine"), Faction.NATO, Category.PISTOL),
			gun("sig_p226", "SIG Sauer P226", "9x19", List.of("sig_p226_magazine"), Faction.NATO, Category.PISTOL),
			gun("colt_m1911a1", "Colt M1911A1", "45_acp", List.of("colt_m1911a1_magazine"), Faction.NATO, Category.PISTOL),
			// SMGs
			gun("hk_mp5", "Heckler & Koch MP5", "9x19", List.of("hk_mp5_magazine"), Faction.NATO, Category.SMG),
			gun("steyr_tmp", "Steyr TMP", "9x19", List.of("steyr_tmp_magazine"), Faction.NATO, Category.SMG),
			gun("uzi", "Uzi", "9x19", List.of("uzi_magazine"), Faction.NATO, Category.SMG),
			// Rifles
			gun("m16a2", "Colt M16A2", "556x45", List.of("stanag_magazine"), Faction.NATO, Category.RIFLE),
			gun("m4_carbine", "M4 Carbine", "556x45", List.of("stanag_magazine"), Faction.NATO, Category.RIFLE),
			gun("hk_g3", "Heckler & Koch G3", "762x51", List.of("hk_g3_magazine"), Faction.NATO, Category.RIFLE),
			gun("fn_fal", "FN FAL", "762x51", List.of("fn_fal_magazine"), Faction.NATO, Category.RIFLE),
			gun("steyr_aug", "Steyr AUG", "556x45", List.of("steyr_aug_magazine"), Faction.NATO, Category.RIFLE),
			gun("m14", "M14", "762x51", List.of("m14_magazine"), Faction.NATO, Category.RIFLE),
			gun("famas", "FAMAS", "556x45", List.of("famas_magazine"), Faction.NATO, Category.RIFLE),
			// Sniper
			gun("remington_700", "Remington 700 / M24", "762x51", List.of("remington_700_magazine"), Faction.NATO, Category.SNIPER),
			gun("barrett_m82", "Barrett M82", "50_bmg", List.of("barrett_m82_magazine"), Faction.NATO, Category.SNIPER),
			// Machine guns
			gun("m240", "M240 (FN MAG)", "762x51", List.of("m240_magazine"), Faction.NATO, Category.MACHINE_GUN),
			// Shotguns
			gun("spas12", "Franchi SPAS-12", "12_gauge", List.of("spas12_magazine"), Faction.NATO, Category.SHOTGUN),
			gun("remington_870", "Remington 870", "12_gauge", List.of("remington_870_magazine"), Faction.NATO, Category.SHOTGUN)
	);

	private GunContent() {
	}

	private static Entry gun(
			String id,
			String displayName,
			String caliberId,
			List<String> acceptedMagazines,
			Faction faction,
			Category category
	) {
		return new Entry(id, displayName, caliberId, acceptedMagazines, faction, category);
	}
}
