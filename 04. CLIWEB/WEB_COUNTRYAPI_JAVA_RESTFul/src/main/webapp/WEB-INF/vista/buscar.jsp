<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
  Buscar y filtrar (ECUD5): nombre y capital parciales y una región. Al elegir otra región el
  formulario se envía solo (data-autoenviar); sin JavaScript, con el botón Buscar.
--%>
<c:set var="titulo" value="Buscar y filtrar"/>
<c:set var="pantalla" value="buscar"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Buscar y filtrar</h1>
<p class="descripcion">Combine nombre, capital y región. Al elegir una región la tabla se actualiza al momento.</p>

<form method="get" action="${ctx}/buscar" data-autoenviar>
    <div class="fila-formulario">
        <div class="campo">
            <label for="nombre">Nombre</label>
            <input id="nombre" name="nombre" value="<c:out value='${param.nombre}'/>">
        </div>
        <div class="campo">
            <label for="capital">Capital</label>
            <input id="capital" name="capital" value="<c:out value='${param.capital}'/>">
        </div>
        <button class="btn" type="submit">Buscar</button>
    </div>
    <fieldset class="chips">
        <legend>Región</legend>
        <label class="chip"><input type="radio" name="region" value="" ${empty param.region ? 'checked' : ''}><span>Todas</span></label>
        <c:forEach var="r" items="${regiones}">
            <label class="chip"><input type="radio" name="region" value="${r}" ${r == param.region ? 'checked' : ''}><span>${r}</span></label>
        </c:forEach>
    </fieldset>
</form>

<div style="margin-top:16px"><%@ include file="partes/avisos.jspf" %></div>

<c:if test="${total != null}">
    <p class="total total--destacado">${total} ${total == 1 ? 'país encontrado' : 'países encontrados'}</p>
</c:if>
<c:if test="${not empty resultados}">
    <div class="tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Capital</th>
                <th scope="col">Región</th>
                <th scope="col">Subregión</th>
                <th scope="col" class="numero">Población</th>
                <th scope="col" class="numero">Área (km²)</th>
                <th scope="col">Moneda</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${resultados}">
                <tr>
                    <td><c:out value="${p.nombre}"/></td>
                    <td><c:out value="${p.capital}"/></td>
                    <td><c:out value="${p.region}"/></td>
                    <td><c:out value="${p.subregion}"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.poblacion}" maxFractionDigits="0"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.area}" maxFractionDigits="2"/></td>
                    <td><c:out value="${p.moneda}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
