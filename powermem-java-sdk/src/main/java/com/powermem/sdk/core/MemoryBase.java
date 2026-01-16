package com.powermem.sdk.core;

import com.powermem.sdk.model.AddMemoryRequest;
import com.powermem.sdk.model.AddMemoryResponse;
import com.powermem.sdk.model.DeleteAllMemoriesRequest;
import com.powermem.sdk.model.DeleteAllMemoriesResponse;
import com.powermem.sdk.model.DeleteMemoryResponse;
import com.powermem.sdk.model.GetAllMemoriesRequest;
import com.powermem.sdk.model.GetAllMemoriesResponse;
import com.powermem.sdk.model.SearchMemoriesRequest;
import com.powermem.sdk.model.SearchMemoriesResponse;
import com.powermem.sdk.model.UpdateMemoryRequest;
import com.powermem.sdk.model.UpdateMemoryResponse;

/**
 * Common memory operations shared by synchronous and asynchronous memory managers.
 *
 * <p>Python reference: {@code src/powermem/core/memory.py} and {@code src/powermem/core/async_memory.py}</p>
 */
public interface MemoryBase {

    AddMemoryResponse add(AddMemoryRequest request);

    SearchMemoriesResponse search(SearchMemoriesRequest request);

    UpdateMemoryResponse update(UpdateMemoryRequest request);

    DeleteMemoryResponse delete(String memoryId, String userId, String agentId);

    GetAllMemoriesResponse getAll(GetAllMemoriesRequest request);

    DeleteAllMemoriesResponse deleteAll(DeleteAllMemoriesRequest request);
}
