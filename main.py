def mostrar_menu():
    print("PAPELERÍA PUNTO & PAPEL")
    print()
    print("1. Registrar producto")
    print("2. Mostrar inventario")
    print("3. Buscar producto")
    print("4. Registrar venta")
    print("5. Salir")


def mostrar_inventario(inventario):
    if inventario == []:
        print("El inventario está vacío. Registra un producto primero.")
    else:
        print("INVENTARIO")
        for producto in inventario:
            print("Nombre:", producto["nombre"])
            print("Precio:", producto["precio"])
            print("Existencias:", producto["existencias"])
            print()


def buscar_producto(inventario):
    if inventario == []:
        print("El inventario está vacío. Registra un producto primero.")
    else:
        nombre_buscado = input("Nombre del producto que buscas: ").strip().lower()
        if nombre_buscado == "":
            print("Escribe un nombre para buscar.")
        else:
            encontrado = False
            for producto in inventario:
                if producto["nombre"].strip().lower() == nombre_buscado:
                    print("Producto encontrado:")
                    print("Nombre:", producto["nombre"])
                    print("Precio:", producto["precio"])
                    print("Existencias:", producto["existencias"])
                    print()
                    encontrado = True
            if encontrado == False:
                print("No se encontró un producto con ese nombre.")


def registrar_producto(inventario):
    nombre = input("Nombre del producto: ").strip()
    if nombre == "":
        print("El nombre del producto no puede estar vacío.")
        return

    try:
        precio = float(input("Precio del producto (ejemplo: 12.50): "))
    except ValueError:
        print("Precio inválido. Escribe un número usando punto para los decimales.")
    else:
        if precio > 0 and precio < float("inf"):
            try:
                existencias = int(input("Cantidad de existencias (unidades enteras): "))
            except ValueError:
                print("Cantidad inválida. Escribe un número entero, como 0, 5 o 10.")
            else:
                if existencias >= 0:
                    producto = {"nombre": nombre, "precio": precio, "existencias": existencias}
                    inventario.append(producto)
                    print("Producto añadido al inventario.")
                    print("Nombre capturado:", producto["nombre"])
                    print("Precio capturado:", producto["precio"])
                    print("Existencias capturadas:", producto["existencias"])
                else:
                    print("Las existencias no pueden ser negativas.")
        else:
            print("El precio debe ser un número finito mayor que cero.")


def registrar_venta(inventario, ventas):
    if inventario == []:
        print("El inventario está vacío. Registra un producto primero.")
        return

    nombre_buscado = input("Nombre del producto que quieres vender: ").strip().lower()
    if nombre_buscado == "":
        print("Escribe el nombre del producto que quieres vender.")
        return

    encontrado = False
    for producto in inventario:
        if producto["nombre"].strip().lower() == nombre_buscado:
            if encontrado == True:
                print("Hay varios productos con ese nombre. No se puede continuar la venta.")
                return
            producto_venta = producto
            print("Producto encontrado para la venta:")
            print("Nombre:", producto["nombre"])
            print("Precio:", producto["precio"])
            print("Existencias:", producto["existencias"])
            print()
            encontrado = True

    if encontrado == False:
        print("No se encontró un producto con ese nombre.")
    else:
        try:
            cantidad = int(input("¿Cuántas unidades quieres vender?: "))
        except ValueError:
            print("Cantidad inválida. Escribe un número entero, como 1, 2 o 3.")
            return

        if cantidad <= 0:
            print("La cantidad a vender debe ser mayor que cero.")
            return

        if cantidad > producto_venta["existencias"]:
            print("No hay suficientes existencias.")
            print("Unidades disponibles:", producto_venta["existencias"])
            return

        print("Hay existencias suficientes para la cantidad solicitada.")
        print("Cantidad solicitada:", cantidad)
        total = producto_venta["precio"] * cantidad
        print(f"Total a pagar: ${total:.2f}")
        producto_venta["existencias"] = producto_venta["existencias"] - cantidad
        print("Existencias actualizadas.")
        print("Unidades restantes:", producto_venta["existencias"])
        venta = {
            "nombre": producto_venta["nombre"],
            "precio_unitario": producto_venta["precio"],
            "cantidad": cantidad,
            "total": total
        }
        ventas.append(venta)
        print("Venta añadida al historial de esta sesión.")


inventario = []
ventas = []
opcion = ""

while opcion != "5":
    mostrar_menu()

    opcion = input("Selecciona una opción: ")

    if opcion == "1":
        registrar_producto(inventario)
    elif opcion == "2":
        mostrar_inventario(inventario)
    elif opcion == "3":
        buscar_producto(inventario)
    elif opcion == "4":
        registrar_venta(inventario, ventas)
    elif opcion == "5":
        print("¡Hasta luego!")
    else:
        print("Opción inválida. Elige un número del 1 al 5.")
