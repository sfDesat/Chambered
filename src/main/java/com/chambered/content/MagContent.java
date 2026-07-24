package com.chambered.content;

import java.util.List;

/**
 * Authoritative roster for magazine (and belt/clip/tube) items.
 * Guns reference these ids via {@link GunContent.Entry#acceptedMagazines()}.
 */
public final class MagContent {
	public record Entry(
			String id,
			String displayName,
			String caliberId,
			int capacity
	) {
		public String ammoTypeId() {
			return caliberId + "_fmj";
		}
	}

	public static final List<Entry> ENTRIES = List.of(
			// Soviet / Warsaw Pact
			mag("makarov_magazine", "PM Magazine", "9x18", 8),
			mag("stechkin_aps_magazine", "APS Magazine", "9x18", 20),
			mag("tokarev_tt33_magazine", "TT-33 Magazine", "762x25", 8),
			mag("pp19_bizon_magazine", "Bizon Helical Magazine", "9x18", 64),
			mag("ak74_magazine", "AK-74 Magazine", "545x39", 30),
			mag("rpk74_magazine", "RPK-74 Magazine", "545x39", 45),
			mag("akm_magazine", "AKM Magazine", "762x39", 30),
			mag("rpk_magazine", "RPK Magazine", "762x39", 40),
			mag("rpd_magazine", "RPD Ammunition Drum", "762x39", 100),
			mag("sks_magazine", "SKS Stripper Clip", "762x39", 10),
			mag("svd_magazine", "SVD Magazine", "762x54r", 10),
			mag("pkm_magazine", "PKM Ammo Box", "762x54r", 100),
			mag("mosin_nagant_magazine", "Mosin Stripper Clip", "762x54r", 5),
			mag("vss_vintorez_magazine", "VSS/VSK Magazine (10)", "9x39", 10),
			mag("vsk94_magazine", "VSS/VSK Magazine (20)", "9x39", 20),
			mag("saiga12_magazine", "Saiga-12 Magazine", "12_gauge", 8),
			// Break-actions still use a 2-shell "mag" item until direct loading exists
			mag("izh43_sawn_off_magazine", "IZh-43 Shells", "12_gauge", 2),
			mag("toz66_sawn_off_magazine", "TOZ-66 Shells", "12_gauge", 2),

			// NATO / UN
			mag("beretta_92fs_magazine", "M9 Magazine", "9x19", 15),
			mag("sig_p226_magazine", "P226 Magazine", "9x19", 15),
			mag("colt_m1911a1_magazine", "1911 Magazine", "45_acp", 7),
			mag("hk_mp5_magazine", "MP5 Magazine", "9x19", 30),
			mag("steyr_tmp_magazine", "TMP Magazine", "9x19", 25),
			mag("uzi_magazine", "Uzi Magazine", "9x19", 32),
			mag("stanag_magazine", "STANAG Magazine", "556x45", 30),
			mag("steyr_aug_magazine", "AUG Magazine", "556x45", 30),
			mag("famas_magazine", "FAMAS Magazine", "556x45", 25),
			mag("hk_g3_magazine", "G3 Magazine", "762x51", 20),
			mag("fn_fal_magazine", "FAL Magazine", "762x51", 20),
			mag("m14_magazine", "M14 Magazine", "762x51", 20),
			mag("remington_700_magazine", "M24 Magazine", "762x51", 5),
			mag("m240_magazine", "M240 Ammo Box", "762x51", 100),
			mag("barrett_m82_magazine", "M82 Magazine", "50_bmg", 10),
			mag("spas12_magazine", "SPAS-12 Shell Tube", "12_gauge", 8),
			mag("remington_870_magazine", "870 Shell Tube", "12_gauge", 7)
	);

	private MagContent() {
	}

	private static Entry mag(String id, String displayName, String caliberId, int capacity) {
		return new Entry(id, displayName, caliberId, capacity);
	}
}
