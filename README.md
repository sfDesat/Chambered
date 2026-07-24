# Chambered

A realistic gun mod for Minecraft **26.2** (Fabric).

## Requirements

- JDK **25**
- Gradle wrapper included (downloads Gradle **9.5.1** automatically)

## Setup

Open this folder in IntelliJ IDEA (or another IDE with Gradle support) and import the Gradle project.

Or from a terminal:

```bat
.\gradlew genSources
.\gradlew build
```

## Run

```bat
.\gradlew runClient
```

## Versions

| Component     | Version                          |
|---------------|----------------------------------|
| Minecraft     | 26.2                             |
| Fabric Loader | 0.19.3                           |
| Fabric API    | 0.155.2+26.2                     |
| GeckoLib      | 5.5.3 (Modrinth `L6bn4TS8`)      |
| Loom          | 1.17-SNAPSHOT                    |
| Gradle        | 9.5.1                            |

## Weapon roster

Placeholder models/geo for everything except the detailed Makarov. Stats and attachment slots are data-driven; refine per gun later.

### Soviet / Warsaw Pact

**Pistols**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Makarov PM | 9×18mm Makarov | muzzle |
| Stechkin APS | 9×18mm Makarov | muzzle, stock |
| Tokarev TT-33 | 7.62×25mm Tokarev | — |

**SMGs**
| Gun | Caliber | Slots |
|-----|---------|-------|
| PP-19 Bizon | 9×18mm Makarov | muzzle, optic, underbarrel, side |
| AKS-74U | 5.45×39mm | muzzle, optic, side |

**Rifles**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Mosin-Nagant | 7.62×54mmR | muzzle, optic |
| SKS | 7.62×39mm | muzzle, optic |
| AK-74 | 5.45×39mm | muzzle, optic, underbarrel, side |
| AKS-74 | 5.45×39mm | muzzle, optic, underbarrel, side |
| AKM | 7.62×39mm | muzzle, optic, underbarrel, side |
| RPK | 7.62×39mm | muzzle, optic, underbarrel, side |
| RPK-74 | 5.45×39mm | muzzle, optic, underbarrel, side |
| OTs-14 Groza | 5.45×39mm | muzzle, optic, underbarrel, side |

**Sniper / DMR**
| Gun | Caliber | Slots |
|-----|---------|-------|
| SVD Dragunov | 7.62×54mmR | muzzle, optic, side |
| VSS Vintorez | 9×39mm | optic, side (integral suppressor) |
| VSK-94 | 9×39mm | muzzle, optic, side |

**Machine guns**
| Gun | Caliber | Slots |
|-----|---------|-------|
| RPD | 7.62×39mm | muzzle, optic, underbarrel, side |
| PKM | 7.62×54mmR | muzzle, optic, underbarrel, side |

**Shotguns**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Saiga-12 | 12 gauge | muzzle, optic, underbarrel, stock, side |
| IZh-43 (sawn-off) | 12 gauge | — |
| TOZ-66 (sawn-off) | 12 gauge | — |

### NATO / UN Forces

**Pistols**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Beretta 92FS / M9 | 9×19mm | muzzle, optic, underbarrel |
| SIG Sauer P226 | 9×19mm | muzzle, optic, underbarrel |
| Colt M1911A1 | .45 ACP | muzzle |

**SMGs**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Heckler & Koch MP5 (A2/A3/SD) | 9×19mm | muzzle, optic, underbarrel, stock, side |
| Steyr TMP | 9×19mm | muzzle, optic, underbarrel, stock |
| Uzi | 9×19mm | muzzle, optic, stock |

**Rifles**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Colt M16A2 | 5.56×45mm | muzzle, optic, underbarrel, side |
| M4 Carbine | 5.56×45mm | muzzle, optic, underbarrel, stock, side |
| Heckler & Koch G3 | 7.62×51mm | muzzle, optic, underbarrel, side |
| FN FAL | 7.62×51mm | muzzle, optic, underbarrel, side |
| Steyr AUG | 5.56×45mm | muzzle, optic, underbarrel |
| M14 | 7.62×51mm | muzzle, optic, underbarrel, side |
| FAMAS | 5.56×45mm | muzzle, optic, underbarrel |

**Sniper**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Remington 700 / M24 | 7.62×51mm | muzzle, optic, underbarrel, side |
| Barrett M82 | .50 BMG | muzzle, optic, underbarrel, side |

**Machine guns**
| Gun | Caliber | Slots |
|-----|---------|-------|
| M240 (FN MAG) | 7.62×51mm | muzzle, optic, underbarrel, side |

**Shotguns**
| Gun | Caliber | Slots |
|-----|---------|-------|
| Franchi SPAS-12 | 12 gauge | muzzle, optic, underbarrel, stock |
| Remington 870 | 12 gauge | muzzle, optic, underbarrel, stock, side |

### Calibers

| Id | Display |
|----|---------|
| `9x18` | 9×18mm Makarov |
| `762x25` | 7.62×25mm Tokarev |
| `545x39` | 5.45×39mm |
| `762x39` | 7.62×39mm |
| `762x54r` | 7.62×54mmR |
| `9x39` | 9×39mm |
| `12_gauge` | 12 Gauge |
| `9x19` | 9×19mm Parabellum |
| `45_acp` | .45 ACP |
| `556x45` | 5.56×45mm NATO |
| `762x51` | 7.62×51mm NATO |
| `50_bmg` | .50 BMG |

Each caliber ships one FMJ (or buckshot) ammo type for now.

## Controls

| Action | Default |
|--------|---------|
| Fire | Left mouse — **semi** = one shot per click; **auto** = hold (RPM-limited) |
| Aim (ADS) | Right mouse (hold) |
| Reload | `R` |
| Inspect | `I` |
| Change fire mode | `B` |
| Field attachments | `V` |
| Debug overlay | `F8` |

## Content layout

```
data/chambered/guns/
data/chambered/magazines/
data/chambered/calibers/
data/chambered/ammo_types/
data/chambered/attachments/

assets/chambered/models/item/gun/<id>/
assets/chambered/geckolib/models/item/<id>.geo.json
assets/chambered/geckolib/animations/item/<id>.animation.json
```

Roster registration lives in `com.chambered.content.GunContent`.
