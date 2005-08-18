/*
 * Created on 14-Jan-2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.proxy.smrj;

import java.io.IOException;

import de.jreality.scene.data.ByteBufferList;
import de.smrj.RemoteCall;
import de.smrj.tcp.TCPReceiverIO;

public class ClientFactory extends de.smrj.ClientFactory
{

  protected void postCall(RemoteCall rc)
  {
    ByteBufferList.BufferPool.releaseAll();
  }

  public static void main(String[] args) throws IOException
  {
    new TCPReceiverIO(args[0], 8868, new ClientFactory()).start();
  }
}
