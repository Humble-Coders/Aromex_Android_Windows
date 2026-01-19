package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.SpecificationRepository
import com.humblecoders.aromex_android_windows.domain.model.Brand
import com.humblecoders.aromex_android_windows.domain.model.Capacity
import com.humblecoders.aromex_android_windows.domain.model.Carrier
import com.humblecoders.aromex_android_windows.domain.model.Color
import com.humblecoders.aromex_android_windows.domain.model.Model
import com.humblecoders.aromex_android_windows.domain.model.Product
import com.humblecoders.aromex_android_windows.domain.model.StorageLocation
import com.humblecoders.aromex_android_windows.domain.model.UnitCost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for all specification-related operations.
 * Handles Brands, Capacities, Carriers, Colors, Models, StorageLocations, UnitCosts, and Products.
 */
class SpecificationViewModel(
    private val specificationRepository: SpecificationRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Local StateFlows for all collections
    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()
    
    private val _capacities = MutableStateFlow<List<Capacity>>(emptyList())
    val capacities: StateFlow<List<Capacity>> = _capacities.asStateFlow()
    
    private val _carriers = MutableStateFlow<List<Carrier>>(emptyList())
    val carriers: StateFlow<List<Carrier>> = _carriers.asStateFlow()
    
    private val _colors = MutableStateFlow<List<Color>>(emptyList())
    val colors: StateFlow<List<Color>> = _colors.asStateFlow()
    
    private val _storageLocations = MutableStateFlow<List<StorageLocation>>(emptyList())
    val storageLocations: StateFlow<List<StorageLocation>> = _storageLocations.asStateFlow()
    
    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()
    
    private val _unitCosts = MutableStateFlow<List<UnitCost>>(emptyList())
    val unitCosts: StateFlow<List<UnitCost>> = _unitCosts.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Per-collection loading states (Improvement #4)
    private val _brandsLoading = MutableStateFlow(false)
    val brandsLoading: StateFlow<Boolean> = _brandsLoading.asStateFlow()
    
    private val _capacitiesLoading = MutableStateFlow(false)
    val capacitiesLoading: StateFlow<Boolean> = _capacitiesLoading.asStateFlow()
    
    private val _carriersLoading = MutableStateFlow(false)
    val carriersLoading: StateFlow<Boolean> = _carriersLoading.asStateFlow()
    
    private val _colorsLoading = MutableStateFlow(false)
    val colorsLoading: StateFlow<Boolean> = _colorsLoading.asStateFlow()
    
    private val _storageLocationsLoading = MutableStateFlow(false)
    val storageLocationsLoading: StateFlow<Boolean> = _storageLocationsLoading.asStateFlow()
    
    private val _unitCostsLoading = MutableStateFlow(false)
    val unitCostsLoading: StateFlow<Boolean> = _unitCostsLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Flags to track if collections have been fetched (to handle empty collections)
    private var hasFetchedBrands = false
    private var hasFetchedCapacities = false
    private var hasFetchedCarriers = false
    private var hasFetchedColors = false
    private var hasFetchedStorageLocations = false
    private var hasFetchedUnitCosts = false
    private var hasFetchedModels = false
    
    // Brands operations
    fun addBrand(brandName: String, onBrandCreated: ((Brand) -> Unit)? = null) {
        if (brandName.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addBrand(Brand(brandName = brandName)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = Brand(id = documentId, brandName = brandName)
                    // Append and keep list sorted
                    val updated = (_brands.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.brandName.lowercase() }
                    _brands.value = updated
                    _isLoading.value = false
                    // Call the callback with the created brand
                    onBrandCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add brand: ${error.message}"
                }
            )
        }
    }
    
    fun updateBrand(brandId: String, brandName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateBrand(brandId, brandName).first()
            result.fold(
                onSuccess = {
                    // Update the brand in the StateFlow
                    val updated = _brands.value.map { brand ->
                        if (brand.id == brandId) {
                            Brand(id = brandId, brandName = brandName)
                        } else {
                            brand
                        }
                    }.sortedBy { it.brandName.lowercase() }
                    _brands.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Brand updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update brand: ${error.message}"
                }
            )
        }
    }
    
    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteBrand(brandId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Brand deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete brand: ${error.message}"
                }
            )
        }
    }
    
    // Capacities operations
    fun addCapacity(capacity: String, onCapacityCreated: ((Capacity) -> Unit)? = null) {
        if (capacity.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addCapacity(Capacity(capacity = capacity)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = Capacity(id = documentId, capacity = capacity)
                    // Append and keep list sorted
                    val updated = (_capacities.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.capacity.lowercase() }
                    _capacities.value = updated
                    _isLoading.value = false
                    // Call the callback with the created capacity
                    onCapacityCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add capacity: ${error.message}"
                }
            )
        }
    }
    
    fun updateCapacity(capacityId: String, capacity: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateCapacity(capacityId, capacity).first()
            result.fold(
                onSuccess = {
                    // Update the capacity in the StateFlow
                    val updated = _capacities.value.map { cap ->
                        if (cap.id == capacityId) {
                            Capacity(id = capacityId, capacity = capacity)
                        } else {
                            cap
                        }
                    }.sortedBy { it.capacity.lowercase() }
                    _capacities.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Capacity updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update capacity: ${error.message}"
                }
            )
        }
    }
    
    fun deleteCapacity(capacityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteCapacity(capacityId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Capacity deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete capacity: ${error.message}"
                }
            )
        }
    }
    
    // Carriers operations
    fun addCarrier(carrierName: String, onCarrierCreated: ((Carrier) -> Unit)? = null) {
        if (carrierName.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addCarrier(Carrier(carrierName = carrierName)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = Carrier(id = documentId, carrierName = carrierName)
                    // Append and keep list sorted
                    val updated = (_carriers.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.carrierName.lowercase() }
                    _carriers.value = updated
                    _isLoading.value = false
                    // Call the callback with the created carrier
                    onCarrierCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add carrier: ${error.message}"
                }
            )
        }
    }
    
    fun updateCarrier(carrierId: String, carrierName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateCarrier(carrierId, carrierName).first()
            result.fold(
                onSuccess = {
                    // Update the carrier in the StateFlow
                    val updated = _carriers.value.map { carrier ->
                        if (carrier.id == carrierId) {
                            Carrier(id = carrierId, carrierName = carrierName)
                        } else {
                            carrier
                        }
                    }.sortedBy { it.carrierName.lowercase() }
                    _carriers.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Carrier updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update carrier: ${error.message}"
                }
            )
        }
    }
    
    fun deleteCarrier(carrierId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteCarrier(carrierId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Carrier deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete carrier: ${error.message}"
                }
            )
        }
    }
    
    // Colors operations
    fun addColor(colorName: String, onColorCreated: ((Color) -> Unit)? = null) {
        if (colorName.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addColor(Color(colorName = colorName)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = Color(id = documentId, colorName = colorName)
                    // Append and keep list sorted
                    val updated = (_colors.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.colorName.lowercase() }
                    _colors.value = updated
                    _isLoading.value = false
                    // Call the callback with the created color
                    onColorCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add color: ${error.message}"
                }
            )
        }
    }
    
    fun updateColor(colorId: String, colorName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateColor(colorId, colorName).first()
            result.fold(
                onSuccess = {
                    // Update the color in the StateFlow
                    val updated = _colors.value.map { color ->
                        if (color.id == colorId) {
                            Color(id = colorId, colorName = colorName)
                        } else {
                            color
                        }
                    }.sortedBy { it.colorName.lowercase() }
                    _colors.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Color updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update color: ${error.message}"
                }
            )
        }
    }
    
    fun deleteColor(colorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteColor(colorId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Color deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete color: ${error.message}"
                }
            )
        }
    }
    
    // Storage Locations operations
    fun addStorageLocation(storageLocation: String, onStorageLocationCreated: ((StorageLocation) -> Unit)? = null) {
        if (storageLocation.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addStorageLocation(StorageLocation(storageLocation = storageLocation)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = StorageLocation(id = documentId, storageLocation = storageLocation)
                    // Append and keep list sorted
                    val updated = (_storageLocations.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.storageLocation.lowercase() }
                    _storageLocations.value = updated
                    _isLoading.value = false
                    // Call the callback with the created storage location
                    onStorageLocationCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add storage location: ${error.message}"
                }
            )
        }
    }
    
    fun updateStorageLocation(storageLocationId: String, storageLocation: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateStorageLocation(storageLocationId, storageLocation).first()
            result.fold(
                onSuccess = {
                    // Update the storage location in the StateFlow
                    val updated = _storageLocations.value.map { sl ->
                        if (sl.id == storageLocationId) {
                            StorageLocation(id = storageLocationId, storageLocation = storageLocation)
                        } else {
                            sl
                        }
                    }.sortedBy { it.storageLocation.lowercase() }
                    _storageLocations.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Storage location updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update storage location: ${error.message}"
                }
            )
        }
    }
    
    fun deleteStorageLocation(storageLocationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteStorageLocation(storageLocationId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Storage location deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete storage location: ${error.message}"
                }
            )
        }
    }
    
    // Models operations
    fun addModel(brandName: String, modelName: String, onModelCreated: ((Model) -> Unit)? = null) {
        if (modelName.isBlank() || brandName.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addModel(Model(brandName = brandName, modelName = modelName)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = Model(id = documentId, brandName = brandName, modelName = modelName)
                    // Append and keep list sorted
                    val updated = (_models.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.modelName.lowercase() }
                    _models.value = updated
                    _isLoading.value = false
                    // Call the callback with the created model
                    onModelCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add model: ${error.message}"
                }
            )
        }
    }
    
    fun updateModel(modelId: String, brandName: String, modelName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateModel(modelId, brandName, modelName).first()
            result.fold(
                onSuccess = {
                    // Update the model in the StateFlow
                    val updated = _models.value.map { model ->
                        if (model.id == modelId) {
                            Model(id = modelId, brandName = brandName, modelName = modelName)
                        } else {
                            model
                        }
                    }.sortedBy { it.modelName.lowercase() }
                    _models.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Model updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update model: ${error.message}"
                }
            )
        }
    }
    
    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteModel(modelId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Model deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete model: ${error.message}"
                }
            )
        }
    }
    
    // Fetch functions
    fun fetchBrands() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedBrands) {
                println("[SpecificationViewModel] ⏭️ Brands already fetched (${_brands.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedBrands = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchBrands().fold(
                onSuccess = { brands ->
                    _brands.value = brands
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch brands: ${error.message}"
                }
            )
        }
    }
    
    fun fetchCapacities() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedCapacities) {
                println("[SpecificationViewModel] ⏭️ Capacities already fetched (${_capacities.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedCapacities = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchCapacities().fold(
                onSuccess = { capacities ->
                    _capacities.value = capacities
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch capacities: ${error.message}"
                }
            )
        }
    }
    
    fun fetchCarriers() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedCarriers) {
                println("[SpecificationViewModel] ⏭️ Carriers already fetched (${_carriers.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedCarriers = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchCarriers().fold(
                onSuccess = { carriers ->
                    _carriers.value = carriers
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch carriers: ${error.message}"
                }
            )
        }
    }
    
    fun fetchColors() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedColors) {
                println("[SpecificationViewModel] ⏭️ Colors already fetched (${_colors.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedColors = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchColors().fold(
                onSuccess = { colors ->
                    _colors.value = colors
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch colors: ${error.message}"
                }
            )
        }
    }
    
    fun fetchStorageLocations() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedStorageLocations) {
                println("[SpecificationViewModel] ⏭️ StorageLocations already fetched (${_storageLocations.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedStorageLocations = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchStorageLocations().fold(
                onSuccess = { storageLocations ->
                    _storageLocations.value = storageLocations
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch storage locations: ${error.message}"
                }
            )
        }
    }
    
    fun fetchModels() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedModels) {
                println("[SpecificationViewModel] ⏭️ Models already fetched (${_models.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedModels = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchModels().fold(
                onSuccess = { models ->
                    _models.value = models
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch models: ${error.message}"
                }
            )
        }
    }
    
    fun fetchUnitCosts() {
        viewModelScope.launch {
            // Skip fetch if already fetched
            if (hasFetchedUnitCosts) {
                println("[SpecificationViewModel] ⏭️ UnitCosts already fetched (${_unitCosts.value.size} items), skipping fetch")
                return@launch
            }
            
            hasFetchedUnitCosts = true
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchUnitCosts().fold(
                onSuccess = { unitCosts ->
                    _unitCosts.value = unitCosts
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch unit costs: ${error.message}"
                }
            )
        }
    }
    
    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            specificationRepository.fetchProducts().fold(
                onSuccess = { products ->
                    _products.value = products
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to fetch products: ${error.message}"
                }
            )
        }
    }
    
    /**
     * Helper data class to represent a collection fetch configuration (Improvement #1)
     */
    private data class CollectionFetchConfig<T>(
        val name: String,
        val stateFlow: MutableStateFlow<List<T>>,
        val loadingFlow: MutableStateFlow<Boolean>,
        val hasFetched: () -> Boolean,
        val setHasFetched: (Boolean) -> Unit,
        val fetchFn: suspend () -> Result<List<T>>
    )
    
    /**
     * Helper function to determine if a collection should be fetched (Improvement #1)
     * Returns the fetch decision without executing the fetch
     */
    private fun <T> shouldFetchCollection(
        config: CollectionFetchConfig<T>,
        forceRefresh: Boolean
    ): Boolean {
        return config.stateFlow.value.isEmpty() && (!config.hasFetched() || forceRefresh)
    }
    
    /**
     * Fetch all specification data (brands, capacities, carriers, colors, storageLocations, unitCosts).
     * Should be called when the purchase screen is opened.
     * Only fetches data if it hasn't been fetched yet (caching).
     * 
     * @param forceRefresh If true, forces a fresh fetch even if data was previously fetched
     */
    fun fetchAllSpecifications(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Check if all data has been fetched at least once
            val allDataFetched = hasFetchedBrands &&
                    hasFetchedCapacities &&
                    hasFetchedCarriers &&
                    hasFetchedColors &&
                    hasFetchedStorageLocations &&
                    hasFetchedUnitCosts
            
            // Also check if data is actually present in StateFlows
            val hasData = _brands.value.isNotEmpty() || _capacities.value.isNotEmpty() || 
                         _carriers.value.isNotEmpty() || _colors.value.isNotEmpty() || 
                         _storageLocations.value.isNotEmpty() || _unitCosts.value.isNotEmpty()
            
            if (allDataFetched && !forceRefresh && hasData) {
                println("[SpecificationViewModel] ⏭️ All specifications already fetched, skipping fetch")
                return@launch
            }
            
            // If data was marked as fetched but is actually empty, reset flags to force fetch
            if (allDataFetched && !hasData) {
                println("[SpecificationViewModel] ⚠️ Data was marked as fetched but is empty, resetting flags and fetching...")
                hasFetchedBrands = false
                hasFetchedCapacities = false
                hasFetchedCarriers = false
                hasFetchedColors = false
                hasFetchedStorageLocations = false
                hasFetchedUnitCosts = false
            }
            
            println("[SpecificationViewModel] 📥 Fetching all specifications...")
            _isLoading.value = true
            _error.value = null
            
            // Create fetch configurations for all collections (Improvement #1)
            val fetchConfigs = listOf(
                CollectionFetchConfig(
                    name = "Brands",
                    stateFlow = _brands,
                    loadingFlow = _brandsLoading,
                    hasFetched = { hasFetchedBrands },
                    setHasFetched = { hasFetchedBrands = it },
                    fetchFn = { specificationRepository.fetchBrands() }
                ),
                CollectionFetchConfig(
                    name = "Capacities",
                    stateFlow = _capacities,
                    loadingFlow = _capacitiesLoading,
                    hasFetched = { hasFetchedCapacities },
                    setHasFetched = { hasFetchedCapacities = it },
                    fetchFn = { specificationRepository.fetchCapacities() }
                ),
                CollectionFetchConfig(
                    name = "Carriers",
                    stateFlow = _carriers,
                    loadingFlow = _carriersLoading,
                    hasFetched = { hasFetchedCarriers },
                    setHasFetched = { hasFetchedCarriers = it },
                    fetchFn = { specificationRepository.fetchCarriers() }
                ),
                CollectionFetchConfig(
                    name = "Colors",
                    stateFlow = _colors,
                    loadingFlow = _colorsLoading,
                    hasFetched = { hasFetchedColors },
                    setHasFetched = { hasFetchedColors = it },
                    fetchFn = { specificationRepository.fetchColors() }
                ),
                CollectionFetchConfig(
                    name = "StorageLocations",
                    stateFlow = _storageLocations,
                    loadingFlow = _storageLocationsLoading,
                    hasFetched = { hasFetchedStorageLocations },
                    setHasFetched = { hasFetchedStorageLocations = it },
                    fetchFn = { specificationRepository.fetchStorageLocations() }
                ),
                CollectionFetchConfig(
                    name = "UnitCosts",
                    stateFlow = _unitCosts,
                    loadingFlow = _unitCostsLoading,
                    hasFetched = { hasFetchedUnitCosts },
                    setHasFetched = { hasFetchedUnitCosts = it },
                    fetchFn = { specificationRepository.fetchUnitCosts() }
                )
            )
            
            // Fetch all collections in parallel (Improvement #2)
            // Create async tasks directly in the coroutine scope to avoid deprecation warning
            val fetchDeferreds = fetchConfigs.map { config ->
                val shouldFetch = shouldFetchCollection(config, forceRefresh)
                
                if (shouldFetch) {
                    async {
                        config.setHasFetched(true)
                        config.loadingFlow.value = true
                        try {
                            val result = config.fetchFn()
                            config.loadingFlow.value = false
                            Pair(result, true) // (result, wasFetched)
                        } catch (e: Exception) {
                            config.loadingFlow.value = false
                            Pair(Result.failure(e), true)
                        }
                    }
                } else {
                    async {
                        if (config.stateFlow.value.isNotEmpty()) {
                            println("[SpecificationViewModel] ⏭️ ${config.name} data exists in StateFlow (${config.stateFlow.value.size} items), skipping fetch")
                        } else {
                            println("[SpecificationViewModel] ⏭️ ${config.name} already fetched, skipping")
                        }
                        Pair(Result.success(config.stateFlow.value), false)
                    }
                }
            }
            
            // Await all fetches to complete
            val fetchResults = fetchDeferreds.awaitAll()
            
            // Batch StateFlow updates (Improvement #3)
            // Only update StateFlows for collections that were actually fetched
            fetchConfigs.zip(fetchResults).forEach { (config, resultPair) ->
                val (result, wasFetched) = resultPair
                if (wasFetched) {
                    when (config.name) {
                        "Brands" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<Brand>>).value = result.getOrNull() as? List<Brand> ?: emptyList()
                        }
                        "Capacities" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<Capacity>>).value = result.getOrNull() as? List<Capacity> ?: emptyList()
                        }
                        "Carriers" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<Carrier>>).value = result.getOrNull() as? List<Carrier> ?: emptyList()
                        }
                        "Colors" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<Color>>).value = result.getOrNull() as? List<Color> ?: emptyList()
                        }
                        "StorageLocations" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<StorageLocation>>).value = result.getOrNull() as? List<StorageLocation> ?: emptyList()
                        }
                        "UnitCosts" -> {
                            @Suppress("UNCHECKED_CAST")
                            (config.stateFlow as MutableStateFlow<List<UnitCost>>).value = result.getOrNull() as? List<UnitCost> ?: emptyList()
                        }
                    }
                }
            }
            
            // Check for errors
            val errors = fetchResults.mapNotNull { (result, _) ->
                result.exceptionOrNull()
            }
            
            _isLoading.value = false
            if (errors.isNotEmpty()) {
                _error.value = "Failed to fetch some data: ${errors.first().message}"
            } else {
                println("[SpecificationViewModel] ✅ All specifications loaded successfully")
            }
        }
    }
    
    /**
     * Fetch models for a specific brand.
     * Should be called when a brand is selected.
     */
    suspend fun fetchModelsByBrand(brandId: String): Result<List<Model>> {
        return specificationRepository.getModelsByBrand(brandId)
    }
    
    fun getModelsByBrand(brandId: String): StateFlow<List<Model>> {
        val brandName = _brands.value.find { it.id == brandId }?.brandName ?: ""
        return _models.map { modelList ->
            modelList.filter { it.brandName == brandName }
        } as StateFlow<List<Model>>
    }
    
    // Unit Costs operations
    fun addUnitCost(unitCost: String, onUnitCostCreated: ((UnitCost) -> Unit)? = null) {
        if (unitCost.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addUnitCost(UnitCost(unitCost = unitCost)).first()
            result.fold(
                onSuccess = { documentId ->
                    val created = UnitCost(id = documentId, unitCost = unitCost)
                    // Append and keep list sorted
                    val updated = (_unitCosts.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.unitCost.lowercase() }
                    _unitCosts.value = updated
                    _isLoading.value = false
                    // Call the callback with the created unit cost
                    onUnitCostCreated?.invoke(created)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add unit cost: ${error.message}"
                }
            )
        }
    }
    
    fun updateUnitCost(unitCostId: String, unitCost: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateUnitCost(unitCostId, unitCost).first()
            result.fold(
                onSuccess = {
                    // Update the unit cost in the StateFlow
                    val updated = _unitCosts.value.map { uc ->
                        if (uc.id == unitCostId) {
                            UnitCost(id = unitCostId, unitCost = unitCost)
                        } else {
                            uc
                        }
                    }.sortedBy { it.unitCost.lowercase() }
                    _unitCosts.value = updated
                    _isLoading.value = false
                    _successMessage.value = "Unit cost updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update unit cost: ${error.message}"
                }
            )
        }
    }
    
    fun deleteUnitCost(unitCostId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteUnitCost(unitCostId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Unit cost deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete unit cost: ${error.message}"
                }
            )
        }
    }
    
    // Products operations
    fun addProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.addProduct(product).first()
            result.fold(
                onSuccess = { documentId ->
                    _isLoading.value = false
                    _successMessage.value = "Product added successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to add product: ${error.message}"
                }
            )
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.updateProduct(product).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Product updated successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to update product: ${error.message}"
                }
            )
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = specificationRepository.deleteProduct(productId).first()
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Product deleted successfully"
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = "Failed to delete product: ${error.message}"
                }
            )
        }
    }
    
    // Helper functions
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
    
}

