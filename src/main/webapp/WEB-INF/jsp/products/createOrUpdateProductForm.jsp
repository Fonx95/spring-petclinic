<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="farmatic" tagdir="/WEB-INF/tags" %>

<farmatic:layout pageName="products">
    <h2>
        <c:if test="${product['new']}">New </c:if> Product
    </h2>
    <form:form modelAttribute="product" class="form-horizontal" id="add-owner-form">
        <div class="form-group has-feedback">
            <farmatic:inputField label="Nombre" name="name"/>
            <farmatic:inputField label="C�digo" name="code"/>
            <farmatic:inputField label="Tipo" name="productType"/>
            <farmatic:inputField label="Precio de vental al p�blico" name="pvp"/>
            <farmatic:inputField label="Precio de venta Farmacia" name="pvf"/>
            <farmatic:inputField label="Stock" name="stock"/>
            <farmatic:inputField label="Stock m�nimo" name="minStock"/>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <c:choose>
                    <c:when test="${product['new']}">
                        <button class="btn btn-default" type="submit">Add Product</button>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-default" type="submit">Update Product</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </form:form>
</farmatic:layout>
