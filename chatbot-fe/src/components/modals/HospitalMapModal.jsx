import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix lỗi icon mặc định của Leaflet trong React
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
});

L.Marker.prototype.options.icon = DefaultIcon;

// Component phụ để tự động điều chỉnh zoom (fitBounds)
const MapBounds = ({ points }) => {
    const map = useMap();
    useEffect(() => {
        if (points && points.length > 0) {
            const bounds = L.latLngBounds(points);
            map.fitBounds(bounds, { padding: [50, 50] });
        }
    }, [map, points]);
    return null;
};

const HospitalMapCard = ({ hospitals, userLocation }) => {
    const handleDirection = (hospital) => {
        // Mở Google Maps chỉ đường từ vị trí người dùng đến bệnh viện
        const url = `https://www.google.com/maps/dir/?api=1&origin=${userLocation.lat},${userLocation.lng}&destination=${hospital.lat},${hospital.lng}&travelmode=driving`;
        window.open(url, '_blank');
    };

    return (
        <div className="w-full rounded-xl overflow-hidden border border-gray-200 shadow-sm bg-white my-2">
            {/* Header */}
            <div className="px-4 py-2 border-b bg-gray-50">
                <h2 className="text-sm font-bold text-gray-800 flex items-center gap-2">
                    📍 Bản đồ bệnh viện lân cận
                </h2>
            </div>
            
            {/* Map Content */}
            <div className="h-64 relative z-0">
                    {userLocation && (
                         <MapContainer 
                            center={[userLocation.lat, userLocation.lng]} 
                            zoom={14} 
                            scrollWheelZoom={false}
                            style={{ height: '100%', width: '100%' }}
                        >
                            {/* Tự động zoom để thấy hết các điểm */}
                            <MapBounds 
                                points={[
                                    [userLocation.lat, userLocation.lng],
                                    ...hospitals.map(h => [h.lat, h.lng])
                                ]} 
                            />
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            />
                            
                            {/* Vị trí người dùng */}
                            <Marker position={[userLocation.lat, userLocation.lng]}>
                                <Popup>
                                    <div className="font-bold text-blue-600">Vị trí của bạn</div>
                                </Popup>
                            </Marker>

                            {/* Danh sách bệnh viện */}
                            {hospitals.map((hospital, index) => (
                                <Marker 
                                    key={index} 
                                    position={[hospital.lat, hospital.lng]}
                                >
                                    <Popup>
                                        <div className="font-semibold text-sm">{hospital.name}</div>
                                        <div className="text-xs text-gray-600 mt-1">
                                            Cách bạn khoảng {calculateDistance(userLocation.lat, userLocation.lng, hospital.lat, hospital.lng).toFixed(2)} km
                                        </div>
                                        <button 
                                            onClick={() => handleDirection(hospital)}
                                            className="mt-2 w-full bg-blue-600 text-white text-xs font-medium py-1.5 px-3 rounded hover:bg-blue-700 transition-colors flex items-center justify-center gap-1"
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                <polygon points="3 11 22 2 13 21 11 13 3 11"></polygon>
                                            </svg>
                                            Chỉ đường
                                        </button>
                                    </Popup>
                                </Marker>
                            ))}
                        </MapContainer>
                    )}
                </div>
        </div>
    );
};

// Hàm tính khoảng cách Haversine (km)
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; 
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = 
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * 
        Math.sin(dLon / 2) * Math.sin(dLon / 2); 
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
    return R * c;
}

export default HospitalMapCard;
