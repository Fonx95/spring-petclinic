<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="farmatic" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<farmatic:layout pageName= "ventas">
	<h2> Venta en Proceso</h2>
	<div class="row no-gutters">
		<div class="col-12 col-sm-6 col-md-8">
			<br>
			<c:if test="${errors != null}">
				<div class="alert alert-danger" role="alert">
					<h4>Error!</h4>
		            <div id="errors" class="errors">
			            <ul>
			            	<c:forEach items="${errors}" var="errorMessage">
		                    	<li><c:out value="${errorMessage.defaultMessage}"/></li>
		                    </c:forEach>
	                    </ul>
		            </div>
		        </div>
	        </c:if>
		</div>
		<div class="col-6 col-md-4">
			<label>Importe Total:</label>
			<input disabled class="marcador" type="text" name="pagado" value="${ventaActual.importeTotal} &#8364"/>
		</div>
	</div>
	<br>
	<table class = "table table-striped">
		<thead>
		<tr>
			<th style="width: 150px">C�digo</th>
			<th style="width: 300px">Nombre</th>
			<th style="width: 175px">Tipo</th>
			<th>PvP</th>
			<th>Stock</th>
			<th style="width: 140px">Cantidad</th>
			<th style="width: 140px">T.A</th>
			<th style="width: 60px">Importe</th>
		</tr>
		</thead>
		<tbody>
			<c:forEach items="${ventaActual.lineaVenta}" var="linea">
				<tr>
					<td>
						<input disabled type="text" class="form-control" name="code" value="${linea.producto.code}"/>
					</td>
					<td>
						<c:out value="${linea.producto.name}"/>
					</td>
					<td>
						<c:out value="${linea.producto.productType}"/>
					</td>
					<td>
						<c:out value="${linea.producto.pvp}"/>
					</td>
					<td>
						<c:out value="${linea.producto.stock}"/>
					</td>
					<td>
						<input disabled type="text" class="form-control" name="Cantidad" value="${linea.cantidad}"/>
					</td>
					<td>
						<input disabled type="text" class="form-control" name="tipoTasa" value="${linea.tipoTasa}"/>
					</td>
					<td>
						<c:out value="${linea.importe}"/>
					</td>
					<td>
						<a class="btn btn-default btn-sm" href="/ventas/actual/${linea.id}">
	                	<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
	                	</a>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${nuevaLinea.producto.code != null}">
				<form:form modelAttribute="nuevaLinea" class="form-horizontal" id="add-linea-form">
					<tr>
						<th>${nuevaLinea.producto.code}</th>
						<th>${nuevaLinea.producto.name}</th>
						<th>${nuevaLinea.producto.productType}</th>
						<th>${nuevaLinea.producto.pvp}</th>
						<th>${nuevaLinea.producto.stock}</th>
						<th>
							<farmatic:inputTypeSimple name="cantidad" type="text" value="${nuevaLinea.cantidad}"/>
						</th>
						<th>
							<select class="form-control" name="tipoTasa">
							<option value="TSI001">TSI001</option>
							<option value="TSI002">TSI002</option>
							<option value="TSI003">TSI003</option>
							<option value="TSI004">TSI004</option>
							<option value="TSI005">TSI005</option>
						</select>
						</th>
						<th>
							<c:out value="${linea.importe}"/>
						</th>
						<th>
							<input type="hidden" name="venta" value="${nuevaLinea.venta.id}"/>
							<input type="hidden" name="Producto" value="${nuevaLinea.producto.id}"/>
							<button class="btn btn-default btn-sm" type="submit">
								<span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
							</button>
						</th>
					</tr>
				</form:form>
			</c:if>
			<form:form modelAttribute="producto" class="form-horizontal" id="search-producto-form">
				<tr>
					<td>
						<div class="form-group ${errorProducto!=null ? 'has-error' : ''}">
							<input class="form-control" name="code" type="text"/>
						</div>
					</td>
					<td>
						<span class="help-inline">${errorProducto}</span>
					</td>
					<td></td>
					<td></td>
					<td></td>
					<td>
					</td>
					<td>
					</td>
					<td></td>
					<td>
						<button class="btn btn-default btn-sm" type="submit">
							<span class="glyphicon glyphicon-search" aria-hidden="true"></span>
						</button>
					</td>
				</tr>
			</form:form>
		</tbody>
	</table>
        
	<a href="/ventas/actual/pagar" class="btn btn-default">Pagar</a>
</farmatic:layout>