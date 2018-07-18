/*
 * Copyright (c) 2018 Xevo Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the names of the copyright holders nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#include "transport_manager/tcp/network_interface_listener_impl.h"
#include "platform_specific_network_interface_listener_impl.h"

namespace transport_manager {
namespace transport_adapter {

CREATE_LOGGERPTR_GLOBAL(logger_, "TransportManager")

NetworkInterfaceListenerImpl::NetworkInterfaceListenerImpl(
    TcpClientListener* tcp_client_listener,
    const std::string designated_interface)
    : platform_specific_impl_(new PlatformSpecificNetworkInterfaceListener(
          tcp_client_listener, designated_interface)) {
  LOG4CXX_AUTO_TRACE(logger_);
}

NetworkInterfaceListenerImpl::~NetworkInterfaceListenerImpl() {
  LOG4CXX_AUTO_TRACE(logger_);
}

bool NetworkInterfaceListenerImpl::Init() {
  LOG4CXX_AUTO_TRACE(logger_);
  return platform_specific_impl_->Init();
}

void NetworkInterfaceListenerImpl::Deinit() {
  LOG4CXX_AUTO_TRACE(logger_);
  platform_specific_impl_->Deinit();
}

bool NetworkInterfaceListenerImpl::Start() {
  LOG4CXX_AUTO_TRACE(logger_);
  return platform_specific_impl_->Start();
}

bool NetworkInterfaceListenerImpl::Stop() {
  LOG4CXX_AUTO_TRACE(logger_);
  return platform_specific_impl_->Stop();
}

}  // namespace transport_adapter
}  // namespace transport_manager
