/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.clientserver;

import java.io.IOException;
import net.rptools.clientserver.hessian.client.MethodClientConnection;
import net.rptools.clientserver.hessian.client.MethodClientDecorator;
import net.rptools.clientserver.hessian.server.MethodServerConnection;
import net.rptools.clientserver.hessian.server.MethodServerDecorator;
import net.rptools.clientserver.simple.client.SocketClientConnection;
import net.rptools.clientserver.simple.client.WebRTCClientConnection;
import net.rptools.clientserver.simple.server.HandshakeProvider;
import net.rptools.clientserver.simple.server.SocketServerConnection;
import net.rptools.clientserver.simple.server.WebRTCServerConnection;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.server.ServerConfig;

public class ConnectionFactory {
  private static ConnectionFactory instance = new ConnectionFactory();

  public static ConnectionFactory getInstance() {
    return instance;
  }

  public MethodClientConnection createClientConnection(String id, ServerConfig config)
      throws IOException {
    if (!AppState.useWebRTC() || config.isPersonalServer())
      return new MethodClientDecorator(
          new SocketClientConnection(id, config.getHostName(), config.getPort()));

    return new MethodClientDecorator(new WebRTCClientConnection(id, config));
  }

  public MethodServerConnection createServerConnection(
      ServerConfig config, HandshakeProvider handshake) throws IOException {
    if (!AppState.useWebRTC() || config.isPersonalServer())
      return new MethodServerDecorator(new SocketServerConnection(config.getPort(), handshake));

    return new MethodServerDecorator(new WebRTCServerConnection(config, handshake));
  }
}