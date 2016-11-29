/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.halyard.controllers.v1;

import com.netflix.spinnaker.halyard.config.model.v1.node.NodeReference;
import com.netflix.spinnaker.halyard.config.model.v1.node.Provider;
import com.netflix.spinnaker.halyard.config.services.v1.ProviderService;
import com.netflix.spinnaker.halyard.config.services.v1.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/config/deployments/{deployment:.+}/providers")
public class ProviderController {
  @Autowired
  UpdateService updateService;

  @Autowired
  ProviderService providerService;

  @RequestMapping(value = "/{provider:.+}", method = RequestMethod.GET)
  Provider provider(
      @PathVariable String deployment,
      @PathVariable String provider,
      @RequestParam(required = false, defaultValue = "false") boolean validate) {
    NodeReference reference = new NodeReference().setDeployment(deployment).setProvider(provider);
    Provider result = providerService.getProvider(reference);

    if (validate) {
      providerService.validateProvider(reference);
    }

    return result;
  }

  @RequestMapping(value = "/{provider:.+}/enabled", method = RequestMethod.PUT)
  void setEnabled(
      @PathVariable String deployment,
      @PathVariable String provider,
      @RequestParam(required = false, defaultValue = "false") boolean validate,
      @RequestBody boolean enabled) {
    NodeReference reference = new NodeReference().setDeployment(deployment).setProvider(provider);
    Runnable doUpdate = () -> { providerService.setEnabled(reference, enabled); };

    Runnable doValidate = () -> { };

    if (validate) {
      doValidate = () -> { providerService.validateProvider(reference); };
    }

    updateService.safeUpdate(doUpdate, doValidate);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  List<Provider> providers(@PathVariable String deployment) {
    NodeReference reference = new NodeReference().setDeployment(deployment);
    return providerService.getAllProviders(reference);
  }
}
