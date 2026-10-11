package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.model.WasteRecordFilter
import javax.inject.Inject

class FilterWasteRecordsUseCase @Inject constructor() {
    operator fun invoke(records: List<WasteRecord>, filter: WasteRecordFilter): List<WasteRecord> =
        if (filter.isActive) records.filter(filter::matches) else records
}