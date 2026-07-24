package com.chambered.attach;

import com.chambered.data.AttachmentDefinition;

/**
 * Where an attachment edit is performed.
 * Field = rail swaps you can do without a bench; workbench = repair / deeper work.
 */
public enum AttachContext {
	FIELD,
	WORKBENCH;

	public boolean allowsAttachment(AttachmentDefinition def) {
		return switch (this) {
			case FIELD -> def.fieldAttachable();
			case WORKBENCH -> true;
		};
	}
}
